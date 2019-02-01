<?php

namespace Jobscore\Repositories\Job;

use Illuminate\Database\Eloquent\Builder;
use Jobscore\Models\Collection;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Repositories\Filters;

class EloquentRepository implements Repository
{
    use Filters;

    public function saveJob(Job $job)
    {
        $job->save();
    }

    public function createJob(array $data) : Job
    {
        return Job::create($data);
    }

    public function deleteJob(Job $job)
    {
        $job->delete();
    }

    public function collectAll() : Collection
    {
        return Job::all();
    }

    public function collectForCompany(Company $company) : Collection
    {
        return Job::where('company_id', '=', $company->getKey())->get();
    }

    public function collectJobs(int $limit = 0, int $offset = 0) : Collection
    {
        $query = $this->getFilteredQuery();

        if ($offset) {
            $query->skip($offset);
        }

        if ($limit) {
            $query->take($limit);
        }

        return $query->get();
    }

    protected function getModel() : string
    {
        return Job::class;
    }

    public function countJobs() : int
    {
        $query = $this->getFilteredQuery();

        return $query->count();
    }

    private function getFilteredQuery() : Builder
    {
        $filters = $this->getFilters();

        $query = Job::query();

        if ($search = $filters['search']) {
            $query->where('name', 'like', "%{$search}%");
        }

        if ($contact = $filters['contact']) {
            $query->whereHas('request.proposals.offers.contact.user', function (Builder $query) use ($contact) {
                $query->whereNotNull('invite_id');
                $query->where('id', '=', $contact);
            });
        }

        if ($company = $filters['company']) {
            $query->where('company_id', '=', $company);
            return $query;
        }
        return $query;
    }

    public function findJobById(string $id) : Job
    {
        return Job::where(Job::HASH_ATTRIBUTE, '=', $id)->first();
    }

    public function existsJobWithId(string $id) : bool
    {
        return (bool) Job::where(Job::HASH_ATTRIBUTE, '=', $id)->count();
    }

    public function collectNeighbours(Job $job) : array
    {
        return [
            'previous'  => null,
            'next'      => null,
        ];
    }
}
