<?php

namespace Jobscore\Http\Controllers;

use Illuminate\Contracts\Auth\StatefulGuard;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;
use Illuminate\Routing\Redirector;
use Illuminate\Translation\Translator;
use Maatwebsite\Excel\ExcelServiceProvider;
use Jobscore\Http\Requests\Job\StoreRequest;
use Jobscore\Http\Requests\Request;
use Jobscore\Models\Collection;
use Jobscore\Models\Company;
use Jobscore\Models\Job;
use Jobscore\Models\User;
use Jobscore\Repositories\Job\PaginatedRepository;
use Jobscore\Repositories\Job\Repository;
use Jobscore\Services\AlertService;
use Jobscore\Services\ExcelService;
use Jobscore\Services\JobService;
use Jobscore\Services\SearchService;
use Prophecy\Argument;
use Prophecy\Prophecy\ObjectProphecy;
use Illuminate\Contracts\View\Factory as ViewFactory;

class JobControllerTest extends \Codeception\Test\Unit
{
    /** @var JobController */
    private $controller;

    /** @var ObjectProphecy */
    private $service;

    /** @var ObjectProphecy */
    private $repository;

    /** @var ObjectProphecy */
    private $view;

    /** @var ObjectProphecy */
    private $redirect;

    /** @var ObjectProphecy */
    private $search;

    /** @var ObjectProphecy */
    private $excel;

    /** @var ObjectProphecy */
    private $guard;

    /** @var ObjectProphecy */
    private $alerts;

    /** @var ObjectProphecy */
    private $lang;

    protected function _before()
    {
        $this->service = $service = $this->prophesize(JobService::class);
        $this->repository = $repository = $this->prophesize(PaginatedRepository::class);
        $this->view = $view = $this->prophesize(ViewFactory::class);
        $this->redirect = $redirect = $this->prophesize(Redirector::class);
        $this->search = $search = $this->prophesize(SearchService::class);
        $this->excel = $excel = $this->prophesize(ExcelService::class);
        $this->guard = $guard = $this->prophesize(StatefulGuard::class);
        $this->alerts = $alerts = $this->prophesize(AlertService::class);
        $this->lang = $lang = $this->prophesize(Translator::class);

        $this->controller = $controller = new JobController(
            $repository->reveal(),
            $service->reveal(),
            $search->reveal(),
            $excel->reveal()
        );

        $controller->setView($view->reveal());
        $controller->setRedirector($redirect->reveal());
        $controller->setGuard($guard->reveal());
        $controller->setAlerts($alerts->reveal());
        $controller->setLang($lang->reveal());
    }

    public function testItIndexesTheJobs()
    {
        // Given
        $company = $this->prophesize(Company::class);
        $request = $this->prophesize(Request::class);
        $jobs = $this->prophesize(Collection::class);
        $user = $this->prophesize(User::class);
        $this->guard->user()->willReturn($user);
        $this->search->getSearch()->willReturn([]);
        $request->grab('filter')->willReturn(['filters']);
        $request->get('export')->willReturn(false);
        $company->getHash()->willReturn('company_id');
        $this->repository->paginate()->shouldBeCalled();
        $this->repository->setFilters(['filters'])->shouldBeCalled();
        $this->repository->setFilter(Argument::type('string'), Argument::any())->willReturn($this->repository);
        $this->repository->collectJobs()->willReturn($jobs);
        $view = $this->prophesize(View::class);
        $this->view->make(JobController::INDEX_VIEW, compact('jobs'))->willReturn($view);

        // When
        $response = $this->controller->index($company->reveal(), $request->reveal());

        // Then
        $this->assertSame($view->reveal(), $response);
    }

    public function testItCreatesAJob()
    {
        // Given
        $create = true;
        $view = $this->prophesize(View::class);
        $this->view->make(JobController::CREATE_VIEW, compact('create'))->willReturn($view);

        // When
        $response = $this->controller->create();

        // Then
        $this->assertSame($view->reveal(), $response);
    }

    public function testItStoresAJob()
    {
        // Given
        $company = $this->prophesize(Company::class);
        $job = $this->prophesize(Job::class);
        $request = $this->prophesize(StoreRequest::class);
        $redirect = $this->prophesize(RedirectResponse::class);
        $request->grab('job')->willReturn(['job']);
        $request->get('continuelater')->willReturn(false);
        $this->service->addJobToCompany(['job'], $company)->willReturn($job);
        $this->lang->trans('job.create.success')->willReturn(['alert']);
        $this->alerts->add(['alert'])->shouldBeCalled();
        $this->redirect->route(JobController::STORE_ROUTE, compact('company'))->willReturn($redirect);

        // When
        $response = $this->controller->store($company->reveal(), $request->reveal());

        // Then
        $this->assertSame($redirect->reveal(), $response);
    }
}
