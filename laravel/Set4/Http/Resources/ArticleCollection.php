<?php

namespace App\Http\Resources;

use Illuminate\Http\Resources\Json\ResourceCollection;

class ArticleCollection extends ResourceCollection
{
    public static $wrap = '';

    /**
     * Transform the resource collection into an array. (For article collection)
     *
     * @param  \Illuminate\Http\Request  $request
     * @return array
     */
    public function toArray($request): array
    {
        return [
            'articles' => $this->collection,
            'articlesCount' => $this->count()
        ];
    }
}
