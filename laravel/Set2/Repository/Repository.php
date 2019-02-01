<?php

namespace Jobscore\Repositories\Job;

use Jobscore\Models\Collection;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Repositories\FilteredRepository;

interface Repository extends FilteredRepository
{
    public function saveJob(Job $job);

    public function createJob(array $data) : Job;

    public function deleteJob(Job $job);

    public function collectAll() : Collection;

    public function collectForCompany(Company $company) : Collection;

    public function collectJobs(int $limit = 0, int $offset = 0) : Collection;

    public function countJobs() : int;

    public function findJobById(string $id) : Job;

    public function existsJobWithId(string $id) : bool;

    public function collectNeighbours(Job $job) : array;
}
