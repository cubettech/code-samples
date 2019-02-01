<?php

namespace Jobscore\Models;

use Jobscore\Models\RFX\Offer;
use Jobscore\Models\RFX\Proposal;
use Jobscore\Models\RFX\Request;
use Illuminate\Database\Eloquent\SoftDeletes;
use Carbon\Carbon;

class Job extends Model
{
    use HasSlug,
        HasDateSelectors,
        Relationships\MorphManyAttachments,
        Relationships\BelongsToCompany,
        Relationships\BelongsToCategory,
        Relationships\HasOneRequest,
        Relationships\BelongsToCity,
        Relationships\BelongsToManyOrganizations,
        Relationships\HasManyAccesses,
        Relationships\BelongsToManySubcategories,
        Relationships\Compliance\HasManyAnswers,
        Relationships\Survey\HasManyEvaluations,
        SoftDeletes;

    const DATE_SELECTOR_FORMAT = 'd/M/Y';
    const DATE_DISPLAY_FORMAT = 'd M Y';
    const RATE_DAILY    = 'daily';
    const RATE_HOURLY   = 'hourly';

    const STATE_INC = 'incomplete';
    const STATE_WAITING = 'waiting';
    const STATE_REJECTED = 'rejected';
    const STATE_ACCEPTED = 'accepted';

    const COMP_POSSIBLE = 'prob_possible';
    const COMP_MUSTCHECK = 'mustcheck';
    const COMP_NOT_POSSIBLE = 'notpossible';

    const FEEDBACK_OK = 'ok';
    const FEEDBACK_IF = 'if';
    const FEEDBACK_NO = 'no';

    public static $rates = [
        self::RATE_DAILY,
        self::RATE_HOURLY,
    ];

    public static $statets = [
        self::STATE_INC,
        self::STATE_WAITING,
        self::STATE_REJECTED,
        self::STATE_ACCEPTED,
    ];

    public static $compliances = [
        self::COMP_POSSIBLE,
        self::COMP_MUSTCHECK,
        self::COMP_NOT_POSSIBLE,
    ];

    public static $feedbacks = [
        self::FEEDBACK_OK,
        self::FEEDBACK_IF,
        self::FEEDBACK_NO,
    ];

    public static $filters = [
        // filter key       => default value
        self::FILTER_SEARCH => '',
        self::FILTER_FIELD  => 'default',
        self::FILTER_SORT   => 'default',
        self::FILTER_ORDER  => 1,
        self::FILTER_AUTH   => null,
        'contact'           => null,
        'company'           => null,
        'dashboard'         => null,
        'categories'        => [],
        'subcategories'     => [],
        'providers'         => [],
        'partners'          => [],
        'organizations'     => [],
        'owners'            => [],
        'countries'         => [],
        'jobs'              => [],
        'cities'            => [],
        'statuses'          => [
            'xactive',
        ],
        'date'              => [
            'min'               => null,
            'max'               => null,
        ],
        'volume'            => [
            'min'               => null,
            'max'               => null,
        ],
        'reco'              => [
            'min'               => null,
            'max'               => null,
        ],
        'score'             => [
            'min'               => null,
            'max'               => null,
        ],
        'state' => [
            'active',
            'inactive'
        ],
    ];

    public static $defaults = [
        self::FILTER_FIELD      => 'all',
        self::FILTER_SORT       => 'created_at',
    ];

    protected $fillable = [
        'number',
        'name',
        'starts_at_selector',
        'ends_at_selector',
        'rate',
        'currency',
        'volume',
        'category',
        'compliance_check',
        'state',
        'feedback',
        'feedback_text',
    ];

    protected $dates = ['starts_at', 'ends_at', 'created_at', 'updated_at', 'deleted_at', 'feedback_at'];

    protected $hidden = [
        'created_at',
        'updated_at',
        'category_id',
        'company_id',
        'request',
        'hash',
        'organization_id',
        'city_id',
        'company',
        'category',
        'viewers',
        'owners',
        'editors',
        'sponsors',
    ];

    protected $bind = 'slug';

    public static $volumes = [
        30000,
        75000,
        125000,
        175000,
        600000,
    ];

    public static $currencies = [
        'EUR',
        'USD',
    ];

    public function viewers()
    {
        return $this->belongsToMany(User::class, Access::TABLE, Access::KEY_JOB, Access::KEY_USER)
            ->wherePivot(Access::RIGHT_ATTR, Access::RIGHT_VIEW);
    }

     /**
     * Method to set Viewers Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 19-08-2018
     * @param Collection $viewers
     * @return Array
     */
    public function setViewersAttribute(Collection $viewers)
    {
        $this->viewers()->sync($viewers->pluck(User::KEY_PRIMARY)->toArray());
    }

    /**
     * Method to get Viewers Selector Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 12-08-2018
     * @return Array
     */
    public function getViewersSelectorAttribute() : array
    {
        return $this->getViewers()->pluck('name', 'hash')->toArray();
    }

    /**
     * Method to get Viewers
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Collection
     */
    public function getViewers() : Collection
    {
        return $this->viewers->merge($this->getEditors());
    }

    /**
     * Method to set Viewers
     * @author Arun <arun.p@cubettech.com>
     * @since 14-08-2018
     * @param Collection $viewers
     * @return Null
     */
    public function setViewers(Collection $viewers)
    {
        $this->viewers = $viewers;
    }

    /**
     * Method to set relation of editors
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Boolean
     */
    public function editors()
    {
        return $this->belongsToMany(User::class, Access::TABLE, Access::KEY_JOB, Access::KEY_USER)
            ->wherePivot(Access::RIGHT_ATTR, Access::RIGHT_EDIT);
    }

    /**
     * Method to set Editors Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 15-08-2018
     * @param Collection $editors
     * @return Null
     */
    public function setEditorsAttribute(Collection $editors)
    {
        $this->editors()->sync($editors->pluck(User::KEY_PRIMARY)->toArray());
    }

    /**
     * Method to get Editors Selector Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getEditorsSelectorAttribute() : array
    {
        return $this->getEditors()->pluck('name', 'hash')->toArray();
    }

    /**
     * Method to get Viewers
     * @author Arun <arun.p@cubettech.com>
     * @since 16-08-2018
     * @return Collection
     */
    public function getEditors() : Collection
    {
        return $this->editors;
    }

    /**
     * Method to set Editors
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $editors
     * @return Null
     */
    public function setEditors(Collection $editors)
    {
        $this->editors = $editors;
    }

    /**
     * Method to set relation for owners
     * @author Arun <arun.p@cubettech.com>
     * @since 13-08-2018
     * @return Boolean
     */
    public function owners()
    {
        return $this->belongsToMany(User::class, Access::TABLE, Access::KEY_JOB, Access::KEY_USER)
            ->wherePivot(Access::TYPE_ATTR, Access::TYPE_OWNER);
    }

    /**
     * Method to set Owners Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $owners
     * @return Array
     */
    public function setOwnersAttribute(Collection $owners)
    {
        $this->owners()->sync($owners->pluck(User::KEY_PRIMARY)->toArray());
    }

    /**
     * Method to get owners
     * @author Arun <arun.p@cubettech.com>
     * @since 20-08-2018
     * @return Collection
     */
    public function getOwners() : Collection
    {
        return $this->owners->unique();
    }

    /**
     * Method to set Owners
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $owners
     * @return Null
     */
    public function setOwners(Collection $owners)
    {
        $this->owners = $owners;
    }

    /**
     * Method to set relation for sponsors
     * @author Arun <arun.p@cubettech.com>
     * @since 17-08-2018
     * @return Null
     */
    public function sponsors()
    {
        return $this->belongsToMany(User::class, Access::TABLE, Access::KEY_JOB, Access::KEY_USER)
            ->wherePivot(Access::TYPE_ATTR, Access::TYPE_SPONSOR);
    }

    /**
     * Method to set Sponsors Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $sponsors
     * @return Null
     */
    public function setSponsorsAttribute(Collection $sponsors)
    {
        $this->sponsors()->sync($sponsors->pluck(User::KEY_PRIMARY)->toArray());
    }

    /**
     * Method to get sponsors
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Collection
     */
    public function getSponsors() : Collection
    {
        return $this->sponsors;
    }

    /**
     * Method to set sponsors
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $sponsors
     * @return Null
     */
    public function setSponsors(Collection $sponsors)
    {
        $this->sponsors = $sponsors;
    }

    /**
     * Method to get Proposal
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param User $user
     * @return Array
     */
    public function getProposal(User $user) : Proposal
    {
        return $this->getProposals()->filter(function (Proposal $proposal) use ($user) {
            return $proposal->getOffers()->filter(function (Offer $offer) use ($user) {
                return $offer->getUser()->is($user);
            })->count();
        })->first();
    }

    /**
     * Check if is accepted
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $viewers
     * @return Boolean
     */
    public function isAccepted() : bool
    {
        return $this->getState() == static::STATE_ACCEPTED;
    }

    protected function getSubcategoriesTable() : string
    {
        return 'job_subcategories';
    }

    protected function getOrganizationsTable() : string
    {
        return 'job_organizations';
    }

    /**
     * Method to Get HTML Title
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getHtmlTitle() : string
    {
        return $this->name;
    }

    /**
     * Method to set Name Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param String $name
     * @return Null
     */
    public function setNameAttribute($name)
    {
        $this->attributes['name'] = $name;
        $this->slug = $this->slugify($name);

        if (empty($name)) {
            $this->slug = $this->slugify(time().'_incomplete');
        }
    }

    /**
     * Method to get Category Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getCategoryDisplayAttribute() : string
    {
        return $this->category ? $this->getCategory()->getName() : '';
    }

    /**
     * Method to get Owners Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getOwnersDisplayAttribute() : string
    {
        return $this->getOwners()->pluck('name')->implode(', ');
    }

    /**
     * Method to get Sponsors Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 19-08-2018
     * @return String
     */
    public function getSponsorsDisplayAttribute() : string
    {
        return $this->getSponsors()->pluck('name')->implode(', ');
    }

    /**
     * Method to get Deadline Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Date
     */
    public function getDeadlineAttribute() : string
    {
        return $this->starts_at->subWeekdays(10)->format(static::DATE_DISPLAY_FORMAT);
    }

    /**
     * Method to Get Daily Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Float
     */
    public function getDailyAttribute()
    {
        return $this->rate == static::RATE_DAILY;
    }

    /**
     * Method to Get Volume
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Collection $viewers
     * @return Int
     */
    public function getVolume() : int
    {
        return $this->volume;
    }

    /**
     * Method to Get Providers
     * @author Arun <arun.p@cubettech.com>
     * @since 24-08-2018
     * @return String
     */
    public function getProviders() : Collection
    {
        return $this->getApprovedProposals()->provider->unique();
    }

    /**
     * Method to Get Proposals
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getProposals() : Collection
    {
        return $this->getRequest()->getProposals();
    }

    /**
     * Method to Check if proposal is frozen
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Boolean
     */
    public function isFroozen()
    {
        if (!$this->getProposals()->count()) {
            return false;
        }

        foreach ($this->getProposals() as $proposal) {
            if ($proposal->frozen()==0)
                return false;
        }

        return true;
    }

    /**
     * Method to get Partners
     * @author Arun <arun.p@cubettech.com>
     * @since 18-08-2018
     * @return String
     */
    public function getPartners() : Collection
    {
        return $this->getProposals()->partners->unique();
    }

    /**
     * Method to get Provider Options Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getPositionOptionsAttribute()
    {
        return $this->getCategory()->getPositions()->filter(function (Position $position) {
            return $position->getCompany()->is($this->getCompany());
        })->groupBy('category_name');
    }

    /**
     * Method to get Provider Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getProvidersDisplayAttribute()
    {
        return $this->getProviders()->pluck('name')->implode(', ');
    }

    /**
     * Method to get Provider List Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 19-08-2018
     * @return Array
     */
    public function getProviderListAttribute()
    {
        return $this->getProviders()->map(function (Provider $provider) {
            $company = $provider->getCompany();
            $proposal = $this->getProviderProposal($provider);
            return [
                'id'            => $provider->getHash(),
                'name'          => $provider->name,
                'link'          => route('provider.edit', compact('company', 'provider')),
                'reco'          => $proposal->reco,
                'score'         => $score = $this->getProviderProposal($provider)->score,
                'score_text'    => trans_choice("survey/evaluation.score.text", $score),
                'submissions'   => $proposal->score_participant_count,
            ];
        });
    }

    /**
     * Method to get Provider Name Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getProviderNameAttribute()
    {
        return $this->getProviders()->name->implode(',');
    }

    /**
     * Method to get Provider Number Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 21-08-2018
     * @return String
     */
    public function getProviderNumberAttribute()
    {
        return $this->getProviders()->number->implode(',');
    }

    /**
     * Method to get Partner Name Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getPartnerNameAttribute()
    {
        return $this->getProviders()->partners->name->implode(',');
    }

    /**
     * Method to get Offers
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getOffers() : Collection
    {
        return $this->getRequest()->getProposals()->offers->unique();
    }

    /**
     * Method to check if provider is selected
     * @author Arun <arun.p@cubettech.com>
     * @since 01-09-2018
     * @param Provider $provider
     * @return Boolean
     */
    public function isSelected(Provider $provider)
    {
        return $this->getRequest()->getProposal($provider)->selected();
    }

    /**
     * Method to get Partners Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getPartnersAttribute()
    {
        return $this->getPartners();
    }

    /**
     * Method to set relationship of dashboard
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function dashboards()
    {
        return $this->belongsToMany(User::class, 'dashboard');
    }

    /**
     * Method to get Dashboards
     * @author Arun <arun.p@cubettech.com>
     * @since 23-08-2018
     * @return Array
     */
    public function getDashboards() : Collection
    {
        return $this->dashboards;
    }

    /**
     * Method to get Dashboard Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Boolean
     */
    public function getDashboardAttribute()
    {
        return (bool) $this->getDashboards()->contains(auth()->user());
    }

    /**
     * Method to get Staff Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Array
     */
    public function getStaffAttribute()
    {
        return $this->getApprovedProposals()->partners->unique();
    }

    /**
     * Method to get Approved Proposals
     * @author Arun <arun.p@cubettech.com>
     * @since 24-08-2018
     * @return Array
     */
    private function getApprovedProposals() : Collection
    {
        return $this->getProposals()->filter(function (Proposal $proposal) {
            return $proposal->selected();
        });
    }

    /**
     * Method to Check Access
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param User $user
     * @param string $type
     * @return Integer
     */
    public function hasAccess(User $user, string $type) : bool
    {
        return (bool) $this->accesses->filter(function (Access $access) use ($user, $type) {
            return $access->type == $type && $access->getUser()->is($user) && $access->getJob($this);
        })->count();
    }

    /**
     * Method to get access details
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param User $user
     * @param string $type
     * @return Array
     */
    public function getAccess(User $user, string $type) : Access
    {
        return $this->accesses->filter(function (Access $access) use ($user, $type) {
            return $access->type == $type && $access->getUser()->is($user) && $access->getJob($this);
        })->first();
    }

    /**
     * Method to get State
     * @author Arun <arun.p@cubettech.com>
     * @since 24-08-2018
     * @return String
     */
    public function getState()
    {
        return $this->state;
    }

    /**
     * Method to set State
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param string $state
     * @return Boolean
     */
    public function setState(string $state)
    {
        $this->state = $state;
    }

    /**
     * Method to do soft delete
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Boolean
     */
    public function softDelete() {
        $this->delete();
    }

    /**
     * Method to get Active date Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 18-08-2018
     * @return Date
     */
    public function getActiveAttribute()
    {
        return Carbon::now()->between($this->getStartsAt(), $this->getEndsAt());
    }

    /**
     * Method to get Provider of Proposal
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @param Provider $provider
     * @return Array
     */
    protected function getProviderProposal(Provider $provider)
    {
        return $this->getProposals()->filter(function (Proposal $proposal) use ($provider) {
            return $proposal->getProvider()->is($provider);
        })->first();
    }

    /**
     * Method to get Updated At Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 24-08-2018
     * @return Date
     */
    public function getUpdatedAtDisplayAttribute()
    {
        if ($this->updated_at->year<1980) {
            return null;
        }

        return $this->updated_at->format('d M Y | H:m');
    }

    /**
     * Method to get person who posted feedback
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return Integer
     */
    public function feedbacker()
    {
        return $this->belongsTo(User::class, 'feedback_by');
    }

    /**
     * Method to get Feedback At Display Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getFeedbackAtDisplayAttribute()
    {
        return $this->feedback_at->format(static::DATE_DISPLAY_FORMAT);
    }
    
    /**
     * Method to set Feedback Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 22-08-2018
     * @param Constant $feedback
     * @return Array
     */
    public function setFeedbackAttribute($feedback)
    {
        if ($feedback == self::FEEDBACK_OK) {
            $this->state = self::STATE_ACCEPTED;
        } elseif ($feedback == self::FEEDBACK_NO) {
            $this->state = self::STATE_REJECTED;
        } elseif ($feedback == self::FEEDBACK_IF) {
            $this->state = self::STATE_WAITING;
        }
        $this->attributes['feedback'] = $feedback;
    }

    /**
     * Method to get Providers Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 11-08-2018
     * @return String
     */
    public function getProvidersAttribute()
    {
        return $this->getApprovedProposals()->provider->unique();
    }

    /**
     * Method to get Contacts Attribute
     * @author Arun <arun.p@cubettech.com>
     * @since 21-08-2018
     * @return String
     */
    public function getContactsAttribute()
    {
        return $this->request->proposals->offers->contact->unique();
    }
}