<?php
namespace App\Repositories;

use Admin\Repositories\UserRepository as AdminUserRepository;
use App\Models\Notifications\NotificationListener;
use App\Models\User;
use App\Repositories\API\ClientUserRepository;
use App\Repositories\Traits\ClientScopeManager;
use App\Support\Entity;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Collection;

class NotificationListenerRepository extends BaseRepository
{
    use ClientScopeManager;

    public $divisions;
    public $teams;
    public $users;

    public function boot()
    {
        parent::boot();

        $this->pushClientScopeCriteria();
    }

    /**
     * @param Builder|Model $query
     * @param Entity $arguments
     * @return Builder
     */
    protected function filter($query, $arguments)
    {
        $query = $query instanceof Model ? $query->newQuery() : $query;

        if ($arguments->get("type"))
        {
            $query->whereIn("type", array_wrap($arguments->get("type")));
        }

        return $query;
    }

    /**
     * @return User[]|Collection
     */
    public function getNotifyTargets()
    {
        $notifiableRoles = applicationRoles()->getAllRoles()
            ->diff(["learner", "author"]);

        $userRepository = new AdminUserRepository($this->getClientScopeCriteria()
            ->getFor());

        $arguments = new Entity(["type" => "all", "scopeRoles" => $notifiableRoles->toArray() , "roles" => $notifiableRoles->toArray() , "paging" => 100000, "groups" => false, ]);

        return $userRepository->get($arguments);
    }

    /**
     * @param Collection $userIds
     * @return Collection|User[]
     */
    protected function getNotifyUsers($userIds)
    {
        $clientScopeCriteria = $this->getClientScopeCriteria();
        return User::query()
            ->whereIn("client_id", array_wrap($clientScopeCriteria->getFor()))
            ->whereIn("id", $userIds->keys()
            ->toArray())
            ->get()
            ->keyBy("id");
    }

    public function model()
    {
        return NotificationListener::class;
    }
}

