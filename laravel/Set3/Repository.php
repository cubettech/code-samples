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

    //
    public function listing($arguments)
    {
        $clientKey = $this->getClientScopeCriteria()->getFor();

        $clientUserRepository = new ClientUserRepository();

        $this->divisions = $clientUserRepository->getDivisions($clientKey);

        $this->teams = $clientUserRepository->getTeams($clientKey);

        $this->model = $this->filter($this->model, $arguments);

        /** @var \Illuminate\Pagination\Paginator $list */
        $list = $this->paginate($arguments->paging);
        $userIds = collect([]);
        $collection = $list->getCollection();

        $collection->each(function ($item) use ($userIds) {
            /** @var \App\Models\Notifications\NotificationListener $item */
            $item->getNotifyUsers()->each(function ($userId) use ($userIds) {
                $userIds->put($userId, $userId);
            });
        });

        $this->users = $this->getNotifyUsers($userIds);

        $collection->each(function ($item) {
            $this->mapGroups($item);

            /** @var \App\Models\Notifications\NotificationListener $item */
            $item->users = collect();

            $item->getNotifyUsers()->each(function ($userId) use ($item) {
                $user = $this->users->get($userId);
                if (!$user) {
                    return;
                }

                $item->users->put($userId, $user);
            });
        });

        return $list;
    }

    /**
     * @param Builder|Model $query
     * @param Entity $arguments
     * @return Builder
     */
    protected function filter($query, $arguments)
    {
        $query = $query instanceof Model ? $query->newQuery() : $query;

        if ($arguments->get("type")) {
            $query->whereIn("type", array_wrap($arguments->get("type")));
        }

        return $query;
    }

    /**
     * @return User[]|Collection
     */
    public function getNotifyTargets()
    {
        $notifiableRoles = applicationRoles()
            ->getAllRoles()
            ->diff(["learner", "author"]);

        $userRepository = new AdminUserRepository(
            $this->getClientScopeCriteria()->getFor()
        );

        $arguments = new Entity([
            "type" => "all",
            "scopeRoles" => $notifiableRoles->toArray(),
            "roles" => $notifiableRoles->toArray(),
            "paging" => 100000,
            "groups" => false,
        ]);

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
            ->whereIn("id", $userIds->keys()->toArray())
            ->get()
            ->keyBy("id");
    }

    protected function mapGroups($item)
    {
        $groups = $item->groups;

        $item->in = collect();

        foreach ($groups->get("divisions", []) as $key) {
            if ($name = $this->divisions->get($key)) {
                $item->in->push(
                    (object) [
                        "type" => "divisions",
                        "id" => $key,
                        "name" => $name,
                    ]
                );
            }
        }

        foreach ($groups->get("teams", []) as $key) {
            if ($name = $this->teams->get($key)) {
                $item->in->push(
                    (object) ["type" => "teams", "id" => $key, "name" => $name]
                );
            }
        }

        return $item;
    }

    public function model()
    {
        return NotificationListener::class;
    }
}
