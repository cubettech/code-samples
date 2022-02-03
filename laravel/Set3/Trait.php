<?php

namespace App\Models\Traits;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Auth;

trait ClientDateAccessor
{
    public static $dateOptions = [
        "default" => "Y-m-d",
        "format" => null,
    ];

    public static function asDateFormat()
    {
        $options = static::getDateOptions();

        return $options["format"];
    }

    public static function getDateFormatted($date, $format = null)
    {
        if ($date) {
            return ($date instanceof Carbon
                ? $date
                : new Carbon($date)
            )->format($format ?: static::asDateFormat());
        }

        return null;
    }

    public static function getDateOptions()
    {
        $options = static::$dateOptions ?? [];

        $options["default"] = $options["default"] ?? "Y-m-d";

        if (null === ($options["format"] ?? null)) {
            $format = trim(Auth::user()->client->date_format ?? null);

            $options["format"] = $format ?: $options["default"];
        }

        return static::$dateOptions = $options;
    }

    public static function getMomentFormat($format)
    {
        return strtr($format, [
            "d" => "DD",
            "D" => "ddd",
            "j" => "D",
            "l" => "dddd",
            "N" => "E",
            "S" => "o",
            "w" => "e",
            "z" => "DDD",
            "W" => "W",
            "F" => "MMMM",
            "m" => "MM",
            "M" => "MMM",
            "n" => "M",
            "t" => "", // no equivalent
            "L" => "", // no equivalent
            "o" => "YYYY",
            "Y" => "YYYY",
            "y" => "YY",
            "a" => "a",
            "A" => "A",
            "B" => "", // no equivalent
            "g" => "h",
            "G" => "H",
            "h" => "hh",
            "H" => "HH",
            "i" => "mm",
            "s" => "ss",
            "u" => "SSS",
            "e" => "zz", // deprecated since version 1.6.0 of moment.js
            "I" => "", // no equivalent
            "O" => "", // no equivalent
            "P" => "", // no equivalent
            "T" => "", // no equivalent
            "Z" => "", // no equivalent
            "c" => "", // no equivalent
            "r" => "", // no equivalent
            "U" => "X",
        ]);
    }

    public function getActivityAtFormattedAttribute()
    {
        return static::getDateFormatted($this->opened_at);
    }

    public function getBirthdateFormattedAttribute()
    {
        if ($this->birthdate) {
            return Carbon::createFromFormat("Y-m-d", $this->birthdate)->format(
                static::getDateFormat()
            );
        }

        return null;
    }

    public function getCompletedAtFormattedAttribute()
    {
        return static::getDateFormatted($this->completed_at);
    }

    public function getCreatedAtFormattedAttribute()
    {
        return static::getDateFormatted($this->created_at);
    }

    public function getDueDateFormattedAttribute()
    {
        return static::getDateFormatted($this->due_date);
    }

    public function getExpiredAtFormattedAttribute()
    {
        return static::getDateFormatted($this->expired_at);
    }

    public function getExpiresAtFormattedAttribute()
    {
        return static::getDateFormatted($this->expires_at);
    }

    public function getExpiringAtFormattedAttribute()
    {
        return static::getDateFormatted($this->expiring_at);
    }

    public function getInvoiceDateFormattedAttribute()
    {
        return static::getDateFormatted($this->invoice_date);
    }

    public function getPaymentDateFormattedAttribute()
    {
        return static::getDateFormatted($this->payment_date);
    }

    public function getPaymentNotificationDateFormattedAttribute()
    {
        return static::getDateFormatted($this->payment_notification_date);
    }

    public function getQualifiedAtFormattedAttribute()
    {
        return static::getDateFormatted($this->qualified_at);
    }

    public function getReleasedFormattedAttribute()
    {
        return static::getDateFormatted($this->released);
    }

    public function getScheduledAtFormattedAttribute()
    {
        return static::getDateFormatted($this->scheduled_at);
    }

    public function getStatusDateFormattedAttribute()
    {
        $dates = collect([
            $this->completed_at,
            $this->aborted_at,
            $this->opened_at,
            $this->deferred_at,
            $this->created_at,
        ]);

        return static::getDateFormatted($dates->filter()->first());
    }

    public function getUpdatedAtFormattedAttribute()
    {
        return static::getDateFormatted($this->updated_at);
    }
}
