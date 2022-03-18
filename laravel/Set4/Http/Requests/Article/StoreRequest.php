<?php

namespace App\Http\Requests\Article;

use Illuminate\Foundation\Http\FormRequest;

class StoreRequest extends FormRequest
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
     * rules to validate before storing an article
     *
     * @return array
     */
    public function rules(): array
    {
        return [
            'article.title' => 'required|string|max:255',
            'article.description' => 'required|string|max:255',
            'article.body' => 'required|string|max:2048',
            'article.tagList' => 'sometimes|array',
            'article.tagList.*' => 'sometimes|string|max:255'
        ];
    }
}
