<?php

namespace App\Http\Controllers;

use App\Http\Requests\Article\DestroyRequest;
use App\Http\Requests\Article\FeedRequest;
use App\Http\Requests\Article\IndexRequest;
use App\Http\Requests\Article\StoreRequest;
use App\Http\Requests\Article\UpdateRequest;
use App\Http\Resources\ArticleCollection;
use App\Http\Resources\ArticleResource;
use App\Models\Article;
use App\Models\User;
use App\Services\ArticleService;

class ArticleController extends Controller
{

    protected Article $article;
    protected ArticleService $articleService;
    protected User $user;

    public function __construct(Article $article, ArticleService $articleService, User $user)
    {
        $this->article = $article;
        $this->articleService = $articleService;
        $this->user = $user;
    }


    /**
    * List the fiitered article collection based on 'tag','author' ,'favorited','limit'
    * and 'offset'
    *
    * @param  App\Http\Requests\Article\IndexRequest $request
    * @return App\Http\Resources\ArticleCollection
    */

    public function index(IndexRequest $request): ArticleCollection
    {
        return new ArticleCollection($this->article->getFiltered($request->validated()));
    }

    /**
    * List the fiitered article collection based on 'limit' and 'offset' for article feeds
    *
    * @param  App\Http\Requests\Article\FeedRequest $request
    * @return App\Http\Resources\ArticleCollection
    */
    public function feed(FeedRequest $request): ArticleCollection
    {
        return new ArticleCollection($this->article->getFiltered($request->validated()));
    }

    /**
     * show complete details of a specific article
     *
     * @param  App\Models\Article
     * @return App\Http\Resources\ArticleResource
     */
    public function show(Article $article): ArticleResource
    {
        return $this->articleResponse($article);
    }

    /**
     * store an article and return the stored article as response
     *
     * @param  App\Http\Requests\Article\StoreRequest $request
     * @return App\Http\Resources\ArticleResource
     */
    public function store(StoreRequest $request): ArticleResource
    {
        $article = auth()->user()->articles()->create($request->validated()['article']);

        $this->articleService->syncTags($article, $request->validated()['article']['tagList'] ?? []);

        return $this->articleResponse($article);
    }

    /**
     * update an existing article and return the updated article as response
     *
     * @param  App\Models\Article
     * @param  App\Http\Requests\Article\UpdateRequest $request
     * @return App\Http\Resources\ArticleResource
     */
    public function update(Article $article, UpdateRequest $request): ArticleResource
    {
        $article->update($request->validated()['article']);

        $this->articleService->syncTags($article, $request->validated()['article']['tagList'] ?? []);

        return $this->articleResponse($article);
    }

    /**
     * destroy / delete an existing article
     *
     * @param  App\Models\Article
     * @param  App\Http\Requests\Article\DestroyRequest $request
     * @return void
     */
    public function destroy(Article $article, DestroyRequest $request): void
    {
        $article->delete();
    }

    /**
     * Mark an article as favourite by a user
     *
     * @param  App\Models\Article
     * @return App\Http\Resources\ArticleResource
     */
    public function favorite(Article $article): ArticleResource
    {
        $article->users()->attach(auth()->id());

        return $this->articleResponse($article);
    }

     /**
     * Mark a favourite article as non-favourite by a user
     *
     * @param  App\Models\Article
     * @return App\Http\Resources\ArticleResource
     */
    public function unfavorite(Article $article): ArticleResource
    {
        $article->users()->detach(auth()->id());

        return $this->articleResponse($article);
    }

    /**
     * Generate formated JSON response of a specific article
     *
     * @param   App\Models\Article
     * @return  App\Http\Resources\ArticleResource
     */
    protected function articleResponse(Article $article): ArticleResource
    {
        return new ArticleResource($article->load('user', 'users', 'tags', 'user.followers'));
    }
}
