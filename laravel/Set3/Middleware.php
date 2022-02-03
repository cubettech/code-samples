<?php

namespace App\Http\Middleware;
use App\Models\Client;
use App\Models\UserRole;
use App\Services\Auth\AuthSwitcherService;
use App\Services\Auth\ClientSwitcherService;
use App\Services\Auth\TokenClientResolverService;
use App\Services\Utils\ClientDomainResolver;
use App\Services\Utils\NotificationService;
use App\Support\Facades\JavaScript;
use Closure;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Auth\Middleware\Authenticate as Base;
use Illuminate\Http\Request;
use RuntimeException;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\AccessDeniedHttpException;

class Authenticate extends Base
{
    protected $domainClient;

    protected $domainResolver;

    protected $isApi;

    protected $isGraphQL;

    protected $isLocal;

    protected $requestClient;

    protected $scopeClient;

    protected $user;


    protected function check($request, $user)
    {
        // staff can skip user/client check
        if ($user && $user->role(UserRole::STAFF_ADMINISTRATOR)) {
            return null;
        }

        $error =
            $this->checkUser($request, $this->user) ?:
            $this->checkClient($this->user);

        if (!$error) {
            return null;
        }

        if ($this->isApi) {
            throw new AuthorizationException($error);
        }

        return redirect(route("login"))->withErrors([
            "email" => [$error],
        ]);
    }

    protected function checkAccess($user)
    {
        if (!$user->can("enrolled", $this->scopeClient)) {
            throw new AccessDeniedHttpException(
                trim(
                    "Access denied for client " .
                        ($this->scopeClient->full_name ?? null)
                )
            );
        }
    }

    protected function checkClient($user)
    {
        $client = $user->client ?? null;

        if ($client && $client->isActive()) {
            return null;
        }

        return ($client->name ?? "Client ") .
            " account is not active. Please contact your manager for further assistance.";
    }

    protected function checkDomain()
    {
        if (!$this->domainClient) {
            return;
        }

        if (
            $this->requestClient !== null &&
            $this->requestClient !== $this->domainClient->id
        ) {
            $append = $this->isApi ? " by this API token" : "";

            throw new AccessDeniedHttpException(
                "You are not allowed to access {$this->domainClient->full_name}{$append}."
            );
        }

        $this->scopeClient = $this->domainClient;
    }

    protected function checkHeader($request)
    {
        if (!$this->requestClient && $request->hasHeader("X-User-Client")) {
            $this->requestClient = (int) $request->header("X-User-Client");

            $this->scopeClient = $this->getClient($this->requestClient);

            if (!$this->scopeClient) {
                throw new RuntimeException(
                    "Invalid client ID `{$this->requestClient}`` provided inside `X-User-Client` header."
                );
            }
        }
    }

    protected function checkOrigin($user)
    {
        $service = resolve(AuthSwitcherService::class);

        $user->self = true;

        if ($origin = $service->getOriginUser()) {
            $user->self = $user->id === $origin->id;
        }

        $user->locked = !($user->self || $origin->can("editProfile", $user));

        return $origin;
    }

    protected function checkToken()
    {
        if ($this->isApi && !($this->isLocal || $this->requestClient)) {
            $this->requestClient = resolve(
                TokenClientResolverService::class
            )->getTokenClientId();

            if ($this->requestClient) {
                $this->scopeClient = $this->getClient($this->requestClient);
            }
        }
    }

    protected function checkUser($request, $user)
    {
        if ($user && $user->isActive()) {
            return null;
        }

        if (
            $request->hasSession() &&
            $request->session()->get("impersonated")
        ) {
            return null;
        }

        return "Account is not active.";
    }

    protected function getClient($key)
    {
        return Client::query()
            ->active()
            ->where("id", $key)
            ->first();
    }

    protected function getClientDomain()
    {
        return $this->domainResolver->getClient();
    }

    public function handle($request, Closure $next, ...$guards)
    {
        $this->isGraphQL = in_array("graphql", $guards, true);

        if ($this->isGraphQL) {
            $guards = ["api"];
        }

        $this->isApi = in_array("api", $guards);

        $this->domainResolver = resolve(ClientDomainResolver::class);

        if (!$this->isApi && $this->auth->guest()) {
            $request->session()->flash("_login", $request->fullUrl());
        }

        if ($this->isLocal = "local" === config("app.env")) {
            $guards = array_diff($guards, ["api"]);
        }

        if ($this->isGraphQL) {
            $result = $this->handleGraph($request, $guards);
        } else {
            $this->authenticate($request, $guards);

            $result = $this->validate($request);
        }

        if ($result ?? null) {
            return $result;
        }

        if (
            $this->scopeClient &&
            $this->domainResolver->isDefaultDomain() &&
            $this->scopeClient->hasCustomDomain()
        ) {
            $redirect = $this->handleRedirect($request);

            if ($redirect) {
                return $redirect;
            }
        }

        $this->setAgent();

        if ($this->isApi || "local" === config("app.env")) {
            Integration::resolve($request);
        }

        return $next($request);
    }

    protected function handleGraph($request, $guards)
    {
        try {
            $this->authenticate($request, $guards);

            $result = $this->validate($request);
        } catch (AuthenticationException $e) {
            // GraphQL query/mutation authorization is handled for each operation
        }

        return $result ?? null;
    }

    protected function handleRedirect(Request $request)
    {
        if ($this->isApi) {
            return null;
        }

        $clientUrl = str_replace(
            $request->getHost(),
            $this->scopeClient->getDomain(),
            $request->url()
        );

        return redirect($clientUrl);
    }

    protected function logout($request)
    {
        auth()->logout();
        if ($request->hasSession()) {
            session()->flush();
            session()->regenerate();
        }
    }

    protected function validate($request)
    {
        $this->user = $request->user();

        if (!$this->user) {
            abort(Response::HTTP_UNAUTHORIZED);
        }

        $this->domainClient = $this->getClientDomain();

        $this->checkHeader($request);

        $this->checkToken();

        $this->checkDomain();

        $this->setNotification();

        $userClient =
            $this->user->activeUserClients
                ->where(
                    "client_id",
                    $this->scopeClient->id ?? $this->user->client_id
                )
                ->first() ?? $this->user->activeUserClients->first();

        if (
            !$this->scopeClient ||
            $this->scopeClient->id !== ($userClient->client_id ?? null)
        ) {
            $this->scopeClient = $userClient->client ?? $this->user->client;

            $this->setClient($this->scopeClient->id ?? null);
        }

        if (!$this->scopeClient) {
            abort(Response::HTTP_NOT_FOUND);
        }

        $this->user->setSelectedClient($this->scopeClient);

        $this->checkAccess($this->user);

        $origin = $this->checkOrigin($this->user);

        return $this->check($request, $origin);
    }

    protected function setAgent(): void
    {
        JavaScript::set("user.client", $this->scopeClient->id ?? 0);
    }

    protected function setClient($client)
    {
        resolve(ClientSwitcherService::class)->setSwitcherClientId(
            $this->user,
            $client
        );
    }

    protected function setNotification()
    {
        resolve(NotificationService::class)->setHeaderNotification();
    }
}
