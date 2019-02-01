<?php

namespace Jobscore\Repositories\Job;

use Carbon\Carbon;
use Jobscore\Models\Collection;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Models\Model;
use Illuminate\Contracts\Cache\Repository as Cache;
use Jobscore\Models\RFX\Offer;
use Jobscore\Models\User;
use Jobscore\Repositories\Filters;
use Jobscore\Repositories\FullCachedRepository as BaseFullCachedRepository;

class FullCachedRepository extends BaseFullCachedRepository implements CachedRepository
{
    /**
     * @var Repository
     */
    private $base;

    protected $keys = [
        'auth',
        ['search', 'field'],
        'contact',
        'company',
        'categories',
        'subcategories',
        'providers',
        'partners',
        'organizations',
        'owners',
        'countries',
        'cities',
        'date',
        'volume',
        'score',
        'reco',
        'dashboard',
        'state'
    ];

    use Filters;

    public function __construct(Cache $cache, Repository $base)
    {
        parent::__construct($cache);
        $this->base = $base;
    }


    protected function getCachePrefix() : string
    {
        return 'job.';
    }

    protected function getCacheKeys(Model $model) : array
    {
        return [
            'collect',
            'collect.all',
            "collect.company.{$model->company_id}",
            "find.id.{$model->hash}",
        ];
    }

    public function saveJob(Job $job)
    {
        return $this->base->saveJob($job);
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
        return $this->cache('collect.all', function () {
            return $this->base->collectAll();
        });
    }

    public function collectForCompany(Company $company) : Collection
    {
        return $this->cache('collect.company.' . $company->getKey(), function () use ($company) {
            return $this->base->collectForCompany($company);
        });
    }

    public function collectJobs(int $limit = 0, int $offset = 0) : Collection
    {
        $filters = $this->getFilters();

        $jobs = $this->cache('collect', function () {
            return $this->base->collectJobs();
        });

        $jobs = $this->filter($jobs, $filters);

        $jobs = $this->sort($jobs, $filters);

        if ($limit) {
            $jobs = $jobs->splice($offset, $limit);
        }

        return $jobs->values();
    }

    protected function getModel() : string
    {
        return Job::class;
    }

    public function countJobs() : int
    {
        $jobs = $this->collectJobs();

        return $jobs->count();
    }

    public function findJobById(string $id) : Job
    {
        return $this->cache("find.id.{$id}", function () use ($id) {
            return $this->base->findJobById($id);
        });
    }

    public function existsJobWithId(string $id) : bool
    {
        return $this->base->existsJobWithId($id);
    }

    protected function filterByAuth(Job $job, User $auth) : bool
    {
        return $auth->can('job.search', compact('job'));
    }

    protected function filterBySearchAndField(Job $job, string $search, string $field) : bool
    {
        return array_reduce($this->getSearchFields($job, $field), function ($carry, $field) use ($search) {
            return $carry || search($search, $field);
        }, false);
    }

    protected function filterByContact(Job $job, string $contact) : bool
    {
        return $job->getOffers()->filter(function (Offer $offer) use ($contact) {
            return $offer->hasInvite() && $offer->getUser()->getHash() == $contact;
        })->count();
    }

    protected function filterByCompany(Job $job, string $company) : bool
    {
        return $job->getCompany()->getHash() == $company;
    }

    protected function filterByCategories(Job $job, array $categories) : bool
    {
        return in_array($job->getCategory()->getKey(), $categories);
    }

    protected function filterBySubcategories(Job $job, array $subcategories) : bool
    {
        return count(array_intersect($job->getSubcategories()->modelKeys(), $subcategories));
    }

    protected function filterByProviders(Job $job, array $providers) : bool
    {
        return count(array_intersect($job->getProviders()->modelHashes(), $providers));
    }

    protected function filterByPartners(Job $job, array $partners) : bool
    {
        return count(array_intersect($job->getPartners()->modelHashes(), $partners));
    }

    protected function filterByOrganizations(Job $job, array $organizations) : bool
    {

        if ($job->getOrganizations()->count() == 0)
        {
            return false;
        }

        foreach ($job->getOrganizations() as $organization)
        {
            if (in_array($organization->getAttributes()['hash'], $organizations))
            {
                return true;
            }
        }

        return false;
    }

    protected function filterByOwners(Job $job, array $owners) : bool
    {
        return count(array_intersect($job->getOwners()->modelHashes(), $owners));
    }

    protected function filterByCountries(Job $job, array $countries) : bool
    {
        return in_array($job->getCountry()->getHash(), $countries);
    }

    protected function filterByCities(Job $job, array $cities) : bool
    {
        return in_array($job->getCity()->getHash(), $cities);
    }

    protected function filterByDate(Job $job, array $dates) : bool
    {
        $min = $dates['min'] ? Carbon::createFromFormat(Job::DATE_SELECTOR_FORMAT, $dates['min'])->startOfDay() : false;
        $max = $dates['max'] ? Carbon::createFromFormat(Job::DATE_SELECTOR_FORMAT, $dates['max'])->endOfDay() : false;

        if (!$min && !$max)
        {
            return true;
        }

        if ($min && !$max)
        {
            return (bool) $job->getStartsAt()->gte($min);
        }

        if (!$min && $max)
        {
            return (bool) $job->getEndsAt()->lte($max);
        }

        if ($min && $max)
        {
            return (bool) ($job->getStartsAt()->gte($min) && $job->getEndsAt()->lte($max));
        }

        return true;

    }

    protected function filterByVolume(Job $job, array $volumes) : bool
    {
        $min = $volumes['min'];
        $max = $volumes['max'];

        return (!$min || $job->getVolume() >= $min) && (!$max || $job->getVolume() <= $max);
    }

    protected function filterByScore(Job $job, array $scores) : bool
    {
        $min = (float) $scores['min'];
        $max = (float) $scores['max'];
        return (!$min || $min == 0.0 || $job->score >= $min) && (!$max || $max == 5.0 || $job->score <= $max);
    }

    protected function filterByReco(Job $job, array $recos) : bool
    {
        $min = (int) $recos['min'];
        $max = (int) $recos['max'];
        return ((!$min || $min == -100) || $job->reco >= $min) && ((!$max || $max == 100) || $job->reco <= $max);
    }

    protected function filterByDashboard(Job $job, string $dashboard) : bool
    {
        return in_array($dashboard, $job->getDashboards()->modelHashes());
    }

    protected function filterByState(Job $job, array $states) : bool
    {
        $inactiveStates = [
            Job::STATE_INC,
            Job::STATE_WAITING,
            Job::STATE_REJECTED,
        ];
        $activeStates = [
            Job::STATE_ACCEPTED,
        ];

        if (count($states) == 2)
        {
            return true;
        }

        if (in_array($job->getState(), $inactiveStates)) {
            $status = 'inactive'; 
        }
        else if (in_array($job->getState(), $activeStates))
        {
            $status = 'active'; 
        }
        else
        {
            $status = '';
        }

        return in_array($status , $states);
    }

    private function getSearchFields(Job $job, string $field) : array
    {
        $map = [
            'name'              => [$job->name],
            'number'            => [$job->number],
            'provider_name'     => $job->getProviders()->name->toArray(),
            'provider_number'   => $job->getProviders()->number->toArray(),
            'partner_name'      => $job->getPartners()->name->toArray(),
            'all'               => array_merge([$job->name, $job->number], $job->getProviders()->name->toArray(), $job->getProviders()->number->toArray(), $job->getPartners()->name->toArray()),
        ];
        return $map[$field];
    }

    public function collectNeighbours(Job $current) : array
    {
        $jobs = $this->collectJobs();

        $next = $jobs->filter(function (Job $job) use ($current) {
            return $job->created_at->gt($current->created_at);
        })->sortBy('created_at', SORT_REGULAR, false)->first();

        $previous = $jobs->filter(function (Job $job) use ($current) {
            return $job->created_at->lt($current->created_at);
        })->sortBy('created_at', SORT_REGULAR, true)->first();

        return compact('previous', 'next');
    }

    public function filter(Collection $collection, array $filters = []) : Collection
    {
        $filteredCollection = parent::filter($collection, $filters);

        $filteredCollection = $filteredCollection->filter(
            function (Model $job) use ($filters)
            {
                if (!empty($filters['statuses']))
                {
                    return $this->filterByState($job, $filters['statuses']);
                }
                return true;
            }
        );

        $filteredCollection = $filteredCollection->filter(
            function (Model $job) use ($filters)
            {
                if (!empty($filters['jobs']))
                {
                    if (in_array($job->getHash(), $filters['jobs']))
                    {
                        return true;
                    }
                    return false;
                }
                return true;
            }
        );

        $filteredCollection = $filteredCollection->filter(
            function (Model $job) use ($filters)
            {
                if (!empty($filters['prefered']))
                {
                    if (!count($job->getProviders()))
                    {
                        return false;
                    } else {
                        return $job->getProviders()->filter(
                            function(Model $provider)
                            {
                                return ($provider->getAttributes()['prefered'] == 'yes');
                            }
                        );
                    }
                }
                return true;
            }
        );

        return $filteredCollection;
    }
}
