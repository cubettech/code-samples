<?php
use Carbon\Carbon as Carbon;
use Ideadrop\Models\Challenge as Challenge;
use Ideadrop\Models\ChallengeScore as ChallengeScore;
use Ideadrop\Models\Filter as Filter;
use Ideadrop\Models\Group as GroupModel;
use Ideadrop\Models\Idea as IdeaModel;
use Ideadrop\Models\IdeaScore as IdeaScore;
use Ideadrop\Models\Intercom as Intercom;
use Ideadrop\Repositories\Bulletin\EloquentBulletinRepository as BulletinRepository;
use Ideadrop\Repositories\Challenge\EloquentChallengeRepository as ChallengeRepository;
use Ideadrop\Repositories\Idea\IdeaRepositoryInterface as IdeaRepository;
use Ideadrop\Repositories\Organisation\EloquentOrganisationRepository as Organisation;
use Ideadrop\Repositories\User\UserRepositoryInterface as UserRepository;
use Illuminate\Support\Collection as Collection;

/**
 * Class containing user dashboard functions
 */
class UsersDashboardController extends \BaseController
{
    protected $user;
    protected $idea;
    protected $ideaScore;
    protected $challengeModel;
    protected $organisation;
    public function __construct(
        Organisation $organisation,
        UserRepository $user,
        IdeaRepository $idea,
        IdeaScore $ideaScore,
        ChallengeScore $challengeScore,
        ChallengeRepository $challenge,
        Challenge $challengeModel,
        Filter $filter,
        BulletinRepository $bulletin
    ) {
        parent::__construct($user);
        $this->idea = $idea;
        $this->user = $user;
        $this->score = $ideaScore;
        $this->chScore = $challengeScore;
        $this->challenge = $challenge;
        $this->challengeModel = $challengeModel;
        $this->organisation = $organisation;
        $this->filter = $filter;
        $this->bulletin = $bulletin;
    }

    /**
     * Method to display dashboard
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-1-2016
     */
    public function index()
    {
        $org = GroupModel::where('id', '=', Session::get("change_org"))->first();
        $charLimit = getCharLimit();

        $ideaActionsCount = $this->user->fetchIdeaActionsCount();
        $bestPracticeCount = $this->idea->bestPracticeCount();

        return View::make('user.dashboard.dashboard', compact(
                'org',
                'ideaActionsCount',
                'bestPracticeCount',
                'charLimit'
            )
        );
    }

    /**
     * Method to fetch top tags
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-1-2016
     */
    public function displayTopHashTags()
    {
        $topHashTags = $this->idea->getTopHashtags();

        $html = View::make('user.dashboard.partials.top-hashtags', compact('topHashTags'))->render();

        return Response::json([
            'status' => 'success',
            'html' => $html
        ]);
    }

    /**
     * Method to fetch action cards
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-1-2016
     */
    public function fetchActionCards()
    {
        $html = '';
        $ideas = $this->user->fetchActionCards();
        $user = Auth::user();

        foreach ($ideas as $idea):
            $html .= View::make('user.dashboard.partials.idea.idea-card', compact('idea', 'user'));
        endforeach;

        return Response::json([
            'status' => 'success',
            'html' => $html
        ]);
    }
    
    /**
     * Method to fetch best cards
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-1-2016
     */
    public function fetchBestPracticeCards()
    {
        $html = '';
        $ideas = $this->user->fetchBestPracticeCards();
        $user = Auth::user();

        foreach ($ideas as $idea):
            $html .= View::make('user.dashboard.partials.idea.idea-card', compact('idea', 'user'));
        endforeach;

        if ($html == '') {
            $html = '<section class="listing_common_box feed-loading">'.Lang::get('general.label-noresultsfound').'</section>';
        }

        return Response::json([
            'status' => 'success',
            'html' => $html
        ]);
    }

    /**
     * Method to fetch new cards
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 12-1-2016
     */
    public function fetchNewCards()
    {
        $user = Auth::user();
        $html = '';
        $cardData = $this->user->fetchNewCards(['type' => Input::get('type')]);
        switch (Input::get('type')) {
            case 'idea':
                $ideas = $cardData;
                foreach ($ideas as $idea):
                    $html .= View::make('user.dashboard.partials.idea.idea-card', compact('idea', 'user'));
                endforeach;
                break;
            case 'challenge':
                $challenges = $cardData;
                foreach ($challenges as $challenge):
                    $html .= View::make('user.dashboard.partials.challenge.challenge-card',
                        compact('challenge', 'user'));
                endforeach;

                break;
            default:
                break;
        }
        return Response::json([
            'status' => 'success',
            'html' => $html
        ]);
    }

    /**
     * Display a listing of the resource.
     * GET /userdashboard
     *
     * @return Response
     *
     * Modified by Aneesh K <aneeshk@cubettech.com>
     * Changed the way to get the search string
     * 5th December, 2014
     */
    public function search()
    {
        $seachKey = Input::get('searchKey');
        $filterBy = Input::get('filterBy') ? Input::get('filterBy') : '';
        $sortBy = Input::get('sortBy') ? Input::get('sortBy') : '';
        $filterType = Input::get('filterType') ? Input::get('filterType') : '';
        $searched = '';
        $closeClass = '';
        if (isset($seachKey) && trim($seachKey) != '') {
            $searched = Input::get('searchKey');
            $closeClass = 'close_X';
        }
        $cards = $this->idea->dashboardCards(Auth::user()->id, htmlspecialchars(str_replace('%', '', $seachKey)),
            $sortBy, $filterBy, 'index');

        return View::make('user.dashboard.index',
            compact('cards', 'searched', 'filterBy', 'sortBy', 'closeClass', 'filterType'));
    }

    /**
     * Method to track the search of user
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 24-03-2015
     */
    public function trackSearch($search_word)
    {
        DB::table('tracker_search')->insert(
            array('user_id' => Auth::user()->id, 'search_word' => $search_word)
        );
        if (isIntercomEnabled()) {
            /* Intercom tracking starts */
            Intercom::createSearchEvent($search_word);
            /* Intercom tracking ends */
        }
    }

    /**
     * Method for ajax filter group search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchGroups()
    {
        $html = $this->filter->getFilterGroupData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter category search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchCategories()
    {
        $html = $this->filter->getFilterCategoryData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter category search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterTypes()
    {
        $selected = Input::get('selected');
        $selected = commaToArray($selected);
        $html = $this->filter->getFilterTypes($selected);
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter user search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchUsers()
    {
        $html = $this->filter->getFilterAuthorData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter user search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchUsersForKanban()
    {
        $html = $this->filter->getFilterAuthorDataForKanban(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter hashtag search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 28-1-2016
     */
    public function getFilterSearchHashTags()
    {
        $html = $this->filter->getFilterHashTagData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter idea-challenges fetch
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchIdeaChallenges()
    {
        $html = $this->filter->getFilterIdeaChallengeData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter idea-status fetch
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchIdeaStatus()
    {
        $html = $this->filter->getFilterIdeaStatusData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax filter challenge-status fetch
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getFilterSearchChallengeStatus()
    {
        $html = $this->filter->getFilterChallengeStatusData(Input::all());
        return json_encode([
            "status" => "success",
            "html" => $html
        ]);
    }

    /**
     * Method for ajax tag search
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function getTagSearch($tagName)
    {
        $orgId = Session::get('change_org');
        $tagId = DB::table('tags')
            ->where('name', '=', $tagName)
            ->where('org_id', '=', $orgId)
            ->pluck('id');
        if ($tagId != '') {
            return Redirect::route('user.feed', ['hashtag_filter' => $tagId]);
        } else {
            return Redirect::route('user.feed');
        }

    }

    /**
     * Method to get org name
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function fetchOrganisationName()
    {
        $orgname = DB::table('groups')->where('id', '=', Session::get('change_org'))->pluck('name');
        return Response::json([
            'orgName' => $orgname
        ]);
    }

    /**
     * Method to fetahc activity widget
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function fetchActivitySection()
    {
        $idea = IdeaModel::findOrFail(Input::get('content_id'));
        $activities = new Collection(Array());
        $lastActivityId = 0;
        $totalCount = 0;
        $html = '';
        $html .= View::make('user.dashboard.partials.idea.idea-activity',
            compact('idea', 'activities', 'lastActivityId', 'totalCount'))->render();
        return Response::json([
            'status' => 'success',
            'html' => $html,
            'data' => [
                'last_id' => 0,
                'total_count' => 0
            ]
        ]);
    }

    /**
     * Method to set the cards viewed
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function markedUserViewed()
    {
        $totalViews = 0;
        switch (Input::get('content_type')) {
            case 'idea':
                $totalViews = $this->idea->setViewed(Input::get('content_id'), Auth::user()->id);
                break;
            case 'challenge':
                $totalViews = $this->challenge->setChallengeViewed(Input::get('content_id'));
                break;
            case 'bulletin':
                $totalViews = $this->bulletin->setBulletinViewed(Input::get('content_id'));
                break;
            default:
                break;
        }
        return Response::json([
            'status' => 'success',
            'totalViews' => $totalViews,
        ]);
    }

    /**
     * Method to end tutorial
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function endTutorial()
    {
        Session::forget('view_tutorial');
        return Response::json([
            'status' => 'success',
            'data' => ''
        ]);
    }

    /**
     * Method to end app tutorial
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function endAppTutorial()
    {
        $user = Auth::user();
        $user->tour = 1;
        $user->save();
        Session::forget('view_tutorial');
        $url = URL::route("user.feed");
        if (Input::get('invite_block') == "1"){
            return Redirect::to($url . '?invite_block=1');
        }else{
            return Redirect::to($url);
        }
    }

    /**
     * Method to accept terms and continue to home
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function acceptTerms()
    {
        $authUserId = Auth::user()->id;
        DB::table('users')
            ->where('id','=',$authUserId)
            ->update([
                'verified' => 1,
                'tour' => 1
            ]);
        Session::forget('view_tutorial');
        Session::forget('privacy_policy');
        return Redirect::route("user.feed");
    }

    /**
     * Method to dispaly terms of use
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function showTerms()
    {
        $authUserId = Auth::user()->id;
        DB::table('users')
            ->where('id','=',$authUserId)
            ->update([
                'verified' => 0,
                'tour' => 0
            ]);
        Session::put('view_tutorial', 1);
        Session::put('privacy_policy', 1);
        return Redirect::route("user.feed");
    }

    /**
     * Method to get departments
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function departments()
    {
        $departments = DB::table('departments')->get();
        return View::make('departments.departments-list', compact('departments'));
    }

    /**
     * Method to get stores
     * @author Rameez Rami <ramees.pu@cubettech.com>
     * @since 20-1-2016
     */
    public function stores()
    {
        $stores = DB::table('stores')->get();
        return View::make('stores.stores-list', compact('stores'));
    }
}