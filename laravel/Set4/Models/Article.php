<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Collection;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Article extends Model
{
    use HasFactory;

    protected $fillable = ['title', 'description', 'body'];

    /**
     * Get the route key for the model.
     *
     * @return string
     */
    public function getRouteKeyName(): string
    {
        return 'slug';
    }

    /**
     * Finds the author of the article
     *
     * @return  Illuminate\Database\Eloquent\Relations\BelongsTo
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Get all tags associated with an article
     *
     * @return  Illuminate\Database\Eloquent\Relations\BelongsToMany
     */
    public function tags(): BelongsToMany
    {
        return $this->belongsToMany(Tag::class);
    }

    /**
     * Get all the users who mark the article as their favourite
     *
     * @return Illuminate\Database\Eloquent\Relations\BelongsToMany
     */
    public function users(): BelongsToMany
    {
        return $this->belongsToMany(User::class);
    }

    /**
     * Get all comments related to the article
     *
     * @return  Illuminate\Database\Eloquent\Relations\HasMany
     */
    public function comments(): HasMany
    {
        return $this->hasMany(Comment::class);
    }

    /**
     * Get filtered articles
     *
     * @param  mixed $filters
     * @return Illuminate\Database\Eloquent\Collection
     */
    public function getFiltered(array $filters): Collection
    {
        return $this->filter($filters, 'tag', 'tags', 'name')
            ->filter($filters, 'author', 'user', 'username')
            ->filter($filters, 'favorited', 'users', 'username')
            ->when(array_key_exists('offset', $filters), function ($q) use ($filters) {
                $q->offset($filters['offset'])->limit($filters['limit']);
            })
            ->with('user', 'users', 'tags', 'user.followers')
            ->get();
    }

}
