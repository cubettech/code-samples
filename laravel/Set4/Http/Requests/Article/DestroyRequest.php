<?php

namespace App\Http\Requests\Article;

use Illuminate\Foundation\Http\FormRequest;

class DestroyRequest extends FormRequest
{
    /**
     * authorize the user
     *
     * @return bool
     */
    public function authorize(): bool
    {
        return $this->route('article')->user->id === auth()->id();
    }

    /**
     * rules to validate delete an article
     *
     * @return array
     */
    public function rules(): array
    {
        return [];
    }
}
