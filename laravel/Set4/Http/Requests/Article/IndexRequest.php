<?php

namespace App\Http\Requests\Article;

use Illuminate\Foundation\Http\FormRequest;

class IndexRequest extends FormRequest
{
    /**
     * authorize the user
     *
     * @return bool
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * rules to validate to get the list of articles
     *
     * @return array
     */
    public function rules(): array
    {
        return [
            'tag' => 'sometimes|string',
            'author' => 'sometimes|string',
            'favorited' => 'sometimes|string',
            'limit' => 'sometimes|integer',
            'offset' => 'sometimes|integer'
        ];
    }
}
