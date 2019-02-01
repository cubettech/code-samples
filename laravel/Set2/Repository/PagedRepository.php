<?php

namespace Jobscore\Repositories\Job;

use Jobscore\Models\Collection;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Repositories\DeferredFilters;
use Jobscore\Repositories\BasePaginatedRepository;

class PagedRepository extends BasePaginatedRepository implements PaginatedRepository
{
    use DeferredFilters;

    /**
     * @var Repository
     */
    protected $base;

    public function __construct(Repository $base)
    {
        $this->base = $base;
    }

    public function saveJob(Job $job)
    {
        $this->base->saveJob($job);
    }

    public function createJob(array $data) : Job
    {
        return $this->base->createJob($data);
    }

    public function deleteJob(Job $job)
    {
        return $this->base->deleteJob($job);
    }

    public function collectAll() : Collection
    {
        return $this->base->collectAll();
    }

    public function collectForCompany(Company $company) : Collection
    {
        return $this->base->collectForCompany($company);
    }

    public function collectJobs(int $limit = 0, int $offset = 0) : Collection
    {
        $page = $this->service->getCurrent();

        $limit = $this->service->getLimit();

        $offset = $offset ?: $limit * ($page - 1);

        $total = $this->countJobs();

        $this->service->setTotal($total);

        return $this->base->collectJobs($limit, $offset);
    }

    public function countJobs() : int
    {
        return $this->base->countJobs();
    }

    public function findJobById(string $id) : Job
    {
        return $this->base->findJobById($id);
    }

    public function existsJobWithId(string $id) : bool
    {
        return $this->base->existsJobWithId($id);
    }

    public function collectNeighbours(Job $job) : array
    {
        return $this->base->collectNeighbours($job);
    }
}
