<?php

namespace App\Listeners\Log;

use App\Events\Document\DocumentCreated as Event;
use App\Jobs\Docs\AddDocumentLog;
use App\Traits\Jobs;

class AddDocumentHistory
{
    use Jobs;

    /**
     * Handle the event.
     *
     * @param  $event
     * @return void
     */
    public function handle(Event $event)
    {
        $message = trans('messages.success.added', ['type' => $event->document->document_number]);

        $this->dispatch(new AddDocumentLog($event->document, 0, $message));
    }
}
