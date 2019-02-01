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

    /**
     * Save Job
     * @author Arun <arun.p@cubettech.com>
     * @since 08-10-2018
     * @param Job $job
     * @return Null
     */
    public function saveJob(Job $job)
    {
        $job->save();
    }

    /**
     * Create Job
     * @author Arun <arun.p@cubettech.com>
     * @since 08-10-2018
     * @param array $data
     * @return Job
     */
    public function createJob(array $data) : Job
    {
        return Job::create($data);
    }

    /**
     * Delete Job
     * @author Arun <arun.p@cubettech.com>
     * @since 08-10-2018
     * @param Job $job
     * @return Null
     */
    public function deleteJob(Job $job)
    {
        $job->delete();
    }

    /**
     * Get all Jobs
     * @author Arun <arun.p@cubettech.com>
     * @since 08-10-2018
     * @return Collection
     */
    public function collectAll() : Collection
    {
        return Job::all();
    }

    /**
     * Jobs For Company
     * @author Arun <arun.p@cubettech.com>
     * @since 15-10-2018
     * @param Company $company
     * @return Collection
     */
    public function collectForCompany(Company $company) : Collection
    {
        return Job::where('company_id', '=', $company->getKey())->get();
    }

    /**
     * Collect Jobs
     * @author Arun <arun.p@cubettech.com>
     * @since 15-10-2018
     * @param int $limit
     * @param int $offset
     * @return Collection
     */
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

    /**
     * Count Jobs
     * @author Arun <arun.p@cubettech.com>
     * @since 10-10-2018
     * @return int
     */
    public function countJobs() : int
    {
        $query = $this->getFilteredQuery();

        return $query->count();
    }

    /**
     * Filters
     * @author Arun <arun.p@cubettech.com>
     * @since 10-10-2018
     * @return Builder
     */
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

    /**
     * Find Job By ID
     * @author Arun <arun.p@cubettech.com>
     * @since 11-10-2018
     * @param string $id
     * @return Job
     */
    public function findJobById(string $id) : Job
    {
        return Job::where(Job::HASH_ATTRIBUTE, '=', $id)->first();
    }

    /**
     * Check if the Job exists
     * @author Arun <arun.p@cubettech.com>
     * @since 11-10-2018
     * @param string $id
     * @return Boolean
     */
    public function existsJobWithId(string $id) : bool
    {
        return (bool) Job::where(Job::HASH_ATTRIBUTE, '=', $id)->count();
    }

    /**
     * Method to Collect Neighbours
     * @author Arun <arun.p@cubettech.com>
     * @since 20-10-2018
     * @param Job $job
     * @return Array
     */
    public function collectNeighbours(Job $job) : array
    {
        return [
            'previous'  => null,
            'next'      => null,
        ];
    }
}
