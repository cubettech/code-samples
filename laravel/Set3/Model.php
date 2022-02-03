<?php

namespace App\Models;

use App\Events\TrainingSessionUpdated;
use App\Models\Media\MediaResources;
use App\Models\Quizzes\Quiz;
use App\Models\Scopes\TrainingSessionQueryScopes;
use App\Models\Traits\Properties;
use App\Models\Traits\Selectable;
use App\Services\Utils\RegistrationIDsGenerator;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Support\Collection;
use Learner\Events\TrainingCompleted;
use Learner\Events\TrainingFailed;
use Learner\Repositories\TrainingRepository;
use App\Models\Traits\ClientDateAccessor;

/**
 *
 * @property TrainingInstance $instance
 * @property int $id
 * @property int $user_id
 * @property int $client_id
 * @property int $training_id
 * @property int $parent_id
 * @property int $instance_id Added when training is completed
 * @property string $registration_id
 * @property int $attempted
 * @property int $score
 * @property int $progress
 * @property string $state
 * @property string $status
 * @property \Carbon\Carbon|null $created_at
 * @property \Carbon\Carbon|null $updated_at
 * @property \Carbon\Carbon|null $deleted_at
 * @property string|null $completed_at
 * @property string|null $opened_at
 * @property string|null $expired_at
 * @property \Carbon\Carbon|null $reseted_at
 * @property-read Client $client
 * @property-read Quiz $quiz
 * @property-read \App\Models\Training $training
 * @property-read User $user
 * @method static bool|null forceDelete()
 * @method static Builder|TrainingSession grouped($keys = array())
 * @method static Builder|TrainingSession asLatest()
 * @method static \Illuminate\Database\Query\Builder|TrainingSession onlyTrashed()
 * @method static bool|null restore()
 * @method static \Illuminate\Database\Query\Builder|TrainingSession withTrashed()
 * @method static \Illuminate\Database\Query\Builder|TrainingSession withoutTrashed()
 * @mixin \Eloquent
 */
class TrainingSession extends Model
{
    use MediaResources, Properties, Selectable, SoftDeletes, ClientDateAccessor;

    const TABLE = "training_sessions";

    const STATUS_COMPLETED = "completed";
    const STATUS_OPEN = "open";

    protected $table = self::TABLE;

    protected $fillable = [
        "user_id",
        "client_id",
        "training_id",
        "parent_id",
        "latest",
        "status",
        "created_at",
        "updated_at",
    ];

    protected $dates = ["deleted_at", "reseted_at"];
    protected $appends = [
        "created_at_formatted",
        "updated_at_formatted",
        "expired_at_formatted",
        "activity_at_formatted",
        "completed_at_formatted",
    ];

    public $timestamps = false;

    /** @var bool Allows disabling progress recalculation after `saved` event */
    public $silent = false;

    public $updateParent = false;

    public $updateProgress = false;

    public $updateState = true;

    protected static function boot()
    {
        parent::boot();

        static::saved(static function (TrainingSession $model) {
            if (true === $model->updateState) {
                $model->updateState()->commit($model->updateParent);
            }
        });
    }

    public function client()
    {
        return $this->hasOne(Client::class, "id", "client_id")->withTrashed();
    }

    public function commit($updateParent = false)
    {
        if ($this->exists && !$this->isDirty()) {
            return $this;
        }

        $timestamps = $this->timestamps;

        $this->timestamps = false;

        $this->saveWithoutEvents();

        $this->timestamps = $timestamps;

        if (false === $this->silent) {
            if ($event = $this->event ?? null) {
                event($event);
            }

            $this->setRelation("event", null);

            event(new TrainingSessionUpdated($this));
        }

        if (true === $updateParent && ($session = $this->updateParent())) {
            $session->commit();
        }

        return $this;
    }

    public function coursewares()
    {
        return $this->hasMany(TrainingCourseware::class, "session_id", "id")
            ->with("progress")
            ->latest();
    }

    public function date($date = null)
    {
        if (is_object($date)) {
            $date = method_exists("date", "toDateTimeString")
                ? $date->toDateTimeString()
                : (string) $date;
        }

        return is_string($date) && (int) $date ? $date : null;
    }

    public function getExpires($completed = null)
    {
        if (null === ($completed = $completed ?? $this->completed_at)) {
            return null;
        }

        $expired = null;

        if (
            ($months = $this->getTrainingProp("months")) &&
            $this->getTrainingProp("type") !== "program"
        ) {
            $calculation = $this->getTrainingProp("calculation");

            $completed = (string) $completed;

            $expired = date(
                "Y-m-d 00:00:00",
                strtotime("+" . $months . " months", strtotime($completed))
            );

            if ($calculation === 2) {
                $expired = date("Y-m-t H:i:s", strtotime($expired));
            } elseif ($calculation === 3) {
                $expired = date(
                    "Y-m-01 H:i:s",
                    strtotime("+1 month", strtotime($expired))
                );
            }
        }

        return $expired;
    }

    /**
     * Returns display representation of the registration ID.
     * @return string
     */
    public function getRegistrationId()
    {
        if (empty($this->registration_id) && $this->quiz) {
            return $this->quiz->getRegistrationId();
        }

        return strtoupper($this->registration_id);
    }

    public function getResets($days = null, $expired = null)
    {
        if (
            !$this->getTrainingProp("recurring") ||
            !($expired = $expired ?: $this->expired_at)
        ) {
            return null;
        }

        $completed =
            $this->completed_at ?:
            $this->updated_at ?:
            $this->created_at ?:
            now()->toDateTimeString();

        $days = $days ?? $this->getTrainingProp("refresh", 0);

        $range = $this->getTrainingProp("months", 0) * 28;

        if ($days && $days >= $range) {
            $days = $range - 1 > 0 ? $range - 1 : 0;
        }

        $resets = strtotime(
            "-" . $days . " days",
            strtotime((string) $expired)
        );

        if (($completed = strtotime((string) $completed)) >= $resets) {
            $resets = $completed;
        }

        return date("Y-m-d 00:00:00", $resets + 1);
    }

    public function getSessionDates($all = false): Collection
    {
        $dates = TrainingCourseware::getCoursewareDates(
            $this->coursewares,
            $all
        )->concat(Quiz::getQuizDates($this->quizzes));

        if (true === $all && $dates->isEmpty()) {
            $dates = $dates->concat(
                $this->only([
                    "created_at",
                    "opened_at",
                    "updated_at",
                    "completed_at",
                ])
            );
        }

        return $dates
            ->filter()
            ->unique()
            ->sort()
            ->values();
    }

    /**
     * @param $value
     * @return Collection
     */
    public function getTrackingAttribute($value)
    {
        return collect($value ? entity($value)->toArray() : []);
    }

    public function getTrainingProp($key, $default = null)
    {
        if (isset($this->attributes[$key])) {
            return $this->attributes[$key] ?? $default;
        }

        return $this->training
            ? $this->training->getAttribute($key) ?? $default
            : $default;
    }

    /**
     * @return \Illuminate\Database\Eloquent\Relations\HasOne
     */
    public function instance()
    {
        return $this->hasOne(TrainingInstance::class, "id", "instance_id");
    }

    public function locked()
    {
        if ($this->completed_at || $this->progress === 100) {
            $this->progress = 100;

            $this->updated_at = $this->completed_at ?: $this->updated_at;

            $this->status = "completed";
        }

        return $this->status === "completed";
    }

    public function parent()
    {
        if ($this->parent_id) {
            $sessions = self::query()
                ->where("user_id", $this->user_id)
                ->where(function ($query) {
                    $query
                        ->where("training_id", $this->parent_id)
                        ->orWhere("parent_id", $this->parent_id);
                })
                ->asLatest()
                ->get()
                ->sortBy("parent_id");

            if (
                ($session = $sessions->shift()) &&
                $session->training_id === $this->parent_id
            ) {
                $session->updateProgress = true;

                $session->setRelation("programSessions", $sessions);

                return $session;
            }
        }

        return null;
    }

    public function program()
    {
        return $this->hasOne(Training::class, "id", "training_id")
            ->program()
            ->withTrashed();
    }

    public function programSessions()
    {
        return $this->hasMany(
            TrainingSession::class,
            "parent_id",
            "training_id"
        )
            //->whereNull('reseted_at')
            ->asLatest();
    }

    /**
     * @return \Illuminate\Database\Eloquent\Relations\HasOne
     */
    public function quiz()
    {
        return $this->hasOne(Quiz::class, "session_id", "id")
            ->orderBy("created_at", "desc")
            ->orderBy("attempt", "desc");
    }

    /**
     * @return \Illuminate\Database\Eloquent\Relations\HasMany
     */
    public function quizzes()
    {
        return $this->hasMany(Quiz::class, "session_id", "id")->latest();
    }

    public function resets($days = null)
    {
        $this->reseted_at = $this->date($this->reseted_at)
            ? $this->reseted_at
            : null;

        $resets = $this->getResets($days);

        if ($reseted = (string) $this->getAttribute("reseted_at") ?: $resets) {
            $completed =
                $this->completed_at ?:
                $this->updated_at ?:
                $this->created_at ?:
                now();

            $expires = $this->getExpires((string) $completed);

            $range = $this->getResets($days, $expires);

            return strtotime($reseted) > strtotime($range) ? $range : $reseted;
        }

        return $resets;
    }

    /**
     * @param \Illuminate\Database\Query\Builder $query
     * @param mixed $date
     * @return \Illuminate\Database\Query\Builder
     */
    public function scopeActive($query, $date = null)
    {
        return TrainingSessionQueryScopes::applyScopeActive($query, $date);
    }

    /**
     * @param \Illuminate\Database\Query\Builder $query
     * @return \Illuminate\Database\Query\Builder
     */
    public function scopeAsLatest($query)
    {
        return $query
            ->where(self::TABLE . ".latest", 1)
            ->whereNull(self::TABLE . ".reseted_at") // @TODO: remove after final tests
            ->whereNull(self::TABLE . ".deleted_at")
            ->orderBy(self::TABLE . ".created_at", "desc");
    }

    /**
     * @param Builder $query
     * @param array $clients
     * @return Builder
     */
    public function scopeClients($query, $clients = [])
    {
        return $query->whereIn(
            self::TABLE . ".client_id",
            array_wrap($clients)
        );
    }

    /**
     * @param Builder $query
     * @param User $user
     * @return Builder
     */
    public function scopeFor($query, $user)
    {
        return $query
            ->where(self::TABLE . ".user_id", $user->id)
            ->where(self::TABLE . ".client_id", $user->client_id);
    }

    /**
     * @param Builder $query
     * @param array $users
     * @param array $clients
     * @return Builder
     */
    public function scopeForUsers($query, $users = [], $clients = [])
    {
        if ($clients) {
            $query->clients($clients);
        }

        return $query->users($users);
    }

    /**
     * @param Builder $query
     * @param array $users
     * @return Builder
     */
    public function scopeUsers($query, $users = [])
    {
        return $query->whereIn(self::TABLE . ".user_id", array_wrap($users));
    }

    /**
     * @param Builder $query
     * @return Builder
     */
    public function scopeWithState($query)
    {
        return $query->with(["coursewares", "quizzes"]);
    }

    /**
     * @param Builder $query
     * @param array $columns
     * @return Builder
     */
    public function scopeWithTraining($query, $columns = [])
    {
        $query->selectable();

        $table = Training::TABLE;

        $columns = array_merge(
            ["type", "months", "recurring", "refresh", "calculation"],
            $columns
        );

        foreach ($columns as $column) {
            $query->addSelect(self::getColumn($column, $table));
        }

        if (!TrainingSessionQueryScopes::joined($query, $table)) {
            $query->join(
                $table,
                self::getColumn("training_id"),
                "=",
                $table . ".id"
            );
        }

        return $query;
    }

    /**
     * @param $value
     * @return mixed
     */
    public function setTrackingAttribute($value)
    {
        return $this->attributes["tracking"] = $value
            ? entity($value)->toJson()
            : "";
    }

    public function training()
    {
        return $this->hasOne(
            Training::class,
            "id",
            "training_id"
        )->withTrashed();
    }

    public function updateDates($updated = null, $created = null)
    {
        if (
            ($created = $this->date($created)) &&
            $created < $this->date($this->created_at)
        ) {
            $this->created_at = $created;
        }

        if (
            ($updated =
                $this->date($updated) ?: $this->date($this->created_at)) &&
            $updated > $this->date($this->updated_at)
        ) {
            $this->updated_at = $updated;
        }

        if ($this->date($this->created_at) > $this->date($this->updated_at)) {
            $this->updated_at = $this->created_at;
        }

        if ($opened = $this->date($this->opened_at)) {
            $opened =
                $opened > $this->date($this->updated_at)
                    ? $this->updated_at
                    : $opened;

            $this->opened_at =
                $opened < $this->date($this->created_at)
                    ? $this->created_at
                    : $opened;
        }

        $now = now()->toDateTimeString();

        foreach (
            [
                "created_at",
                "opened_at",
                "updated_at",
                "completed_at",
                "deleted_at",
            ]
            as $key
        ) {
            if (
                ($date = $this->date($this->get($key))) &&
                strtotime($date) > time()
            ) {
                $date = $now;
            }

            $this->setAttribute($key, $date ?: null);
        }

        if ($completed = $this->date($this->completed_at)) {
            $this->updated_at = $completed;
        }

        return $this;
    }

    public function updateExpired()
    {
        if (null === $this->completed_at) {
            $this->expired_at = null;

            return $this;
        }

        if ($this->expired_at) {
            return $this;
        }

        $this->expired_at = $this->getExpires();

        return $this;
    }

    public function updateParent($updateProgress = null)
    {
        if (null === ($programSession = $this->parent())) {
            return null;
        }

        $programSession->updateProgress =
            $updateProgress ?? $this->updateProgress;

        return $programSession->updateState();
    }

    public function updateProgram()
    {
        if (null === $this->program) {
            return $this;
        }

        $keys = ($program = $this->program) ? $program->hasModules->all() : [];

        if (!$this->relationLoaded("programSessions")) {
            $sessions = $this->programSessions()
                ->whereIn("training_id", $keys)
                ->where("user_id", $this->user_id)
                ->asLatest()
                ->get();

            $this->setRelation("programSessions", $sessions);
        }

        $sessions = $this->programSessions
            ->where("user_id", $this->user_id)
            ->whereIn("training_id", $keys)
            ->keyBy("training_id")
            ->values();

        $dates = collect()
            ->push($sessions->pluck("created_at"))
            ->push($sessions->pluck("updated_at"))
            ->push($sessions->pluck("completed_at"))
            ->collapse()
            ->filter()
            ->sort();

        if ($range = count($keys) * 100) {
            $score = $sessions->sum("progress");

            $progress = (int) (($score / $range) * 100);

            if (0 === $progress && $score) {
                $progress = 1;
            }
        }

        $this->progress = $progress ?? 0;

        $this->created_at = $dates->first() ?: $this->created_at;

        $this->updated_at = $dates->last() ?: $this->created_at;

        $this->opened_at = $this->progress === 0 ? null : $this->opened_at;

        $this->completed_at =
            $this->progress === 100 ? $this->updated_at : null;

        return $this;
    }

    public function updateProgress()
    {
        if ($this->program) {
            return $this->updateProgram();
        }

        if ($this->locked()) {
            return $this;
        }

        $user = (object) [
            "id" => $this->user_id,
            "client_id" => $this->client_id,
        ];

        $training = (new TrainingRepository($user))->session($this);

        if ($state = $training->state ?? null) {
            $this->progress = (int) $state->progress;
        }

        return $this;
    }

    public function updateReseted($days = null)
    {
        $this->expired_at = $this->date($this->expired_at)
            ? $this->expired_at
            : null;

        $this->reseted_at = $this->date($this->reseted_at)
            ? $this->reseted_at
            : null;

        if ($this->reseted_at || $this->expired_at === null) {
            return $this;
        }

        if (
            ($resets = $this->getResets($days)) &&
            strtotime($resets) <= time()
        ) {
            $this->setAttribute("reseted_at", $resets);
        }

        return $this;
    }

    public function updateQuiz()
    {
        if (null === ($quiz = $this->quiz)) {
            return $this;
        }

        if ($registration = $quiz->registration_id) {
            $this->registration_id = $registration;
        }

        $this->attempted = $quiz->attempt ?: 1;

        $this->score = (int) $quiz->get("score", 0);

        $this->state = $quiz->status ?: "open";

        $this->updateDates($quiz->updated_at, $quiz->created_at);

        if ("failed" === ($quiz->event ?? null)) {
            $this->setRelation("event", new TrainingFailed($this));
        }

        return $this;
    }

    public function updateState($updated = null, $created = null)
    {
        $this->setRelation("event", null);

        if (true === $this->updateProgress) {
            $this->updateQuiz();

            $this->updateProgress();
        }

        $this->updateDates($updated, $created);

        $this->updateStatus();

        $this->updateExpired();

        $this->updateReseted();

        return $this;
    }

    public function updateStatus()
    {
        if (($progress = $this->progress) && !$this->opened_at) {
            $this->opened_at =
                $this->created_at === $this->updated_at
                    ? now()
                    : $this->updated_at;
        }

        $this->instance_id = $this->instance_id ?: 0;

        $this->registration_id = $this->registration_id ?: "";

        if (!$this->status && $progress) {
            $this->status = "open";
        }

        $this->status = $this->status ?: "";

        if (null === $this->completed_at && $progress !== 100) {
            return $this;
        }

        $completed = $this->completed_at;

        if (!$this->instance_id) {
            $this->instance_id =
                $this->training && $this->training->instance
                    ? $this->training->instance->id
                    : 0;
        }

        if (
            !$this->registration_id &&
            $this->getTrainingProp("type") !== "program"
        ) {
            $this->registration_id =
                resolve(RegistrationIDsGenerator::class)->generateForModel(
                    $this,
                    "registration_id"
                ) ?:
                "";
        }

        $this->status = "completed";

        $this->completed_at = $completed ?: $this->updated_at;

        if (!$completed) {
            $this->setRelation("event", new TrainingCompleted($this));
        }

        return $this;
    }

    public function user()
    {
        return $this->hasOne(User::class, "id", "user_id")->withTrashed();
    }
}
