<?php

namespace App\Http\Requests\Article;

use Illuminate\Foundation\Http\FormRequest;

class FeedRequest extends FormRequest
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
     * rules to validate to feed the list of articles
     *
     * @return array
     */
    public function rules(): array
    {
        return [
            'limit' => 'sometimes|integer',
            'offset' => 'sometimes|integer'
        ];
    }
}
