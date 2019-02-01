<?php

namespace Jobscore\Http\Controllers;

use Carbon\Carbon;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse as Redirect;
use Jobscore\Http\Requests\Job\ApproveRequest;
use Jobscore\Http\Requests\Job\SearchRequest;
use Jobscore\Http\Requests\Job\StoreRequest;
use Jobscore\Http\Requests\Job\UpdateRequest;
use Jobscore\Http\Requests\Request;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Repositories\Job\PaginatedRepository;
use Jobscore\Services\ExcelService;
use Jobscore\Services\JobService;
use Jobscore\Services\SearchService;
use Jobscore\Repositories\Provider\PaginatedRepository as ProviderRepository;

class JobController extends Controller
{
    const INDEX_VIEW        = 'job.index';
    const CREATE_VIEW       = 'job.create';
    const STORE_ROUTE       = 'job.index';
    const EDIT_VIEW         = 'job.edit';
    const UPDATE_ROUTE      = 'job.edit';
    const SEARCH_ROUTE      = 'job.index';
    const PIN_ROUTE         = 'job.index';
    const SHOW_VIEW         = 'job.show';

    /**
     * @var PaginatedRepository
     */
    private $repository;

    /**
     * @var Provider PaginatedRepository
     */
    private $providerRepository;

    /**
     * @var JobService
     */
    private $service;

    /**
     * @var SearchService
     */
    private $search;

    /**
     * @var ExcelService
     */
    private $excel;

    public function __construct(
        PaginatedRepository $repository,
        JobService $service,
        SearchService $search,
        ExcelService $excel,
        ProviderRepository $providerRepository
    ) {
        $this->repository = $repository;
        $this->service = $service;
        $this->search = $search;
        $this->excel = $excel;
        $this->providerRepository = $providerRepository;
    }

    /**
     * Method to list Jobs
     * @author Arun <arun.p@cubettech.com>
     * @since 08-10-2018
     * @param Company $company
     * @param Request $request
     * @return View
     */
    public function index(Company $company, Request $request)
    {
        $filters = array_merge($this->search->getSearch(), $request->grab('filter'));

        if (!empty($filters['sort']))
        {
            switch ($filters['sort'])
            {
                case 'name':
                    $filters['order'] = false;
                    break;
                case 'number':
                    $filters['order'] = false;
                    break;
                case 'created_at':
                    $filters['order'] = true;
                    break;
            }
        }

        $this->repository->setFilters($filters);
        $this->repository->setFilter('auth', $this->user());
        $this->repository->setFilter('company', $company->getHash());
        $this->repository->paginate();

        $jobs = $this->repository->collectJobs();

        if ($request->get('export')) 
        {
            $this->excel->view('Jobs', 'excel.jobs', compact('company', 'jobs'));
        }

        return $this->view->make(static::INDEX_VIEW, compact('jobs'));
    }

    /**
     * Method to search Jobs
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-10-2018
     * @param SearchRequest $request
     * @return Redirect
     */
    public function search(SearchRequest $request)
    {
        $this->search->setSearch($request->grab('search'));
        return $this->redirect->back();
    }

    /**
     * Method to create Company
     * @author Arun <arun.p@cubettech.com>
     * @since 13-10-2018
     * @param Company $company
     * @return View
     */
    public function create(Company $company)
    {
        $create = true;

        return $this->view->make(static::CREATE_VIEW, compact('create', 'company'));
    }

    /**
     * Method to clone Job
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 13-10-2018
     * @param Company $company
     * @param Job $job
     * @return View
     */
    public function createClone(Company $company, Job $job)
    {
        $clone = true;

        return $this->view->make(static::CREATE_VIEW, compact('company', 'job', 'clone'));
    }

    /**
     * Method to store Job
     * @author Arun <arun.p@cubettech.com>
     * @since 16-10-2018
     * @param Company $company
     * @param StoreRequest $request
     * @return Redirect
     */
    public function store(Company $company, StoreRequest $request)
    {
        $data = $request->grab('job');

        if ($request->input('submit') == 'continuelater') {
            $data['state'] = Job::STATE_INC;
        } else {
            $data['state'] = Job::STATE_WAITING;
        }

        $this->service->addJobToCompany($data, $company);
        $this->alerts->add($this->lang->trans('job.create.success'));
        return $this->redirect->route(static::STORE_ROUTE, compact('company'));
    }

    /**
     * Method to edit Job
     * @author Arun <arun.p@cubettech.com>
     * @since 16-10-2018
     * @param Company $company
     * @param Job $job
     * @return View
     */
    public function edit(Company $company, Job $job)
    {
        return $this->view->make(static::EDIT_VIEW, compact('job', 'providers'));
    }

    /**
     * Method to update Request
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-10-2018
     * @param Company $company
     * @param Job $job
     * @param UpdateRequest $request
     * @return View
     */
    public function update(Company $company, Job $job, UpdateRequest $request)
    {
        $data = $request->grab('job');

        if ($request->input('submit') == 'continuelater') {
            $data['state'] = Job::STATE_INC;
        } else {
            $data['state'] = Job::STATE_WAITING;
        }

        $this->service->updateJob($job, $data);
        $tab = $request->input('submit');
        $this->alerts->add($this->lang->trans('job.edit.success'));
        return $this->redirect->route('job.index', compact('company'));
    }

    /**
     * Method to pin Job
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 24-10-2018
     * @param Company $company
     * @param Job $job
     * @return Redirect
     */
    public function pin(Company $company, Job $job)
    {
        $user = $this->user();
        if ($user->getDashboard()->contains($job)) {
            $user->dashboard()->detach($job);
        } else {
            $user->dashboard()->attach($job);
        }
        return $this->redirect->back();
    }

    /**
     * Method to export Job
     * @author Arun <arun.p@cubettech.com>
     * @since 20-10-2018
     * @param Company $company
     * @param Job $job
     * @param ApproveRequest $request
     * @return Redirect
     */
    public function approve(Company $company, Job $job, ApproveRequest $request) {
        $this->service->setState($job, $request->input('state'));
        return $this->redirect->back();
    }

    /**
     * Method to delete Job
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-10-2018
     * @param Company $company
     * @param Job $job
     * @return Redirect
     */
    public function delete(Company $company, Job $job)
    {
        $job->softDelete();
        return $this->redirect->back();
    }

    /**
     * Method to show Job
     * @author Arun <arun.p@cubettech.com>
     * @since 19-10-2018
     * @param Company $company
     * @param Job $job
     * @return View
     */
    public function show(Company $company, Job $job)
    {
        $this->repository->setFilter('auth', $this->user());
        $this->repository->setFilter('company', $company->getHash());
        $this->repository->setFilter('state', [$job->getState()]);
        $neighbours = $this->repository->collectNeighbours($job);
        return $this->view->make(static::SHOW_VIEW, compact('company', 'job', 'neighbours'));
    }

    /**
     * Method to export Job
     * @author Arun <arun.p@cubettech.com>
     * @since 20-10-2018
     * @param Company $company
     * @param Job $job
     * @return File
     */
    public function export(Company $company, Job $job)
    {
        return $this->excel->pdf('Job', 'excel.job', compact('company', 'job'));
    }

    /**
     * Method to review Job
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-11-2018
     * @param Company $company
     * @param Job $job
     * @param Request $request
     * @return Redirect
     */
    public function review(Company $company, Job $job, Request $request)
    {
        $job->fill($request->grab('job'));
        $job->feedbacker()->associate($this->user());
        $job->feedback_at = Carbon::now();
        $job->save();
        return $this->redirect->back();
    }
}