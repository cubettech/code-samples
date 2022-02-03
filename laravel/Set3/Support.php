<?php

namespace App\Support;

use DateTime;
use DateTimeZone;
use Illuminate\Support\Collection;

class Timezones
{
    /**
     * @var Collection
     */
    private static $listIdentifiers;

    /**
     * @return \Illuminate\Support\Collection
     */
    public static function list()
    {
        $items = collect();

        $date = new DateTime();

        foreach (self::getDateTimeZoneList() as $timezone) {
            $date->setTimezone($timezone->timezone);
            $items->put($timezone->id, "UTC/GMT " . $date->format("P"));
        }

        return $items;
    }

    /**
     * @return \Illuminate\Support\Collection
     */
    public static function namedList()
    {
        return self::getDateTimeZoneList()->mapWithKeys(function ($timezone) {
            return [
                $timezone->id => self::displayTimezoneName($timezone),
            ];
        });
    }

    /**
     * @return Collection
     */
    public static function groupedList()
    {
        return self::getDateTimeZoneList()
            ->map(function ($item) {
                return (new Entity($item))->set(
                    "name",
                    self::displayTimezoneName($item, true)
                );
            })
            ->groupBy("region");
    }

    /**
     * Returns a array of allowed timezone values
     * @return \Illuminate\Support\Collection
     */
    public static function keys()
    {
        return self::list()->keys();
    }

    /**
     * @param DateTimeZone|string $timezone
     * @return mixed
     */
    public static function getTimezoneName($timezone)
    {
        if ($timezone instanceof DateTimeZone) {
            $timezone = $timezone->getName();
        }

        $timezoneObject = self::getDateTimeZoneList()->get($timezone);

        return self::displayTimezoneName($timezoneObject);
    }

    /**
     * @return Collection|Entity[]
     */
    public static function getDateTimeZoneList()
    {
        if (!self::$listIdentifiers) {
            self::$listIdentifiers = self::generateTimezonesList();
        }

        return self::$listIdentifiers;
    }

    protected static function generateTimezonesList()
    {
        $date = new DateTime();

        return collect(DateTimeZone::listIdentifiers())
            ->map(function ($id) use ($date) {
                $nameArray = explode("/", $id);
                $timezone = new DateTimeZone($id);
                $date->setTimezone($timezone);
                $region = str_replace("_", " ", array_shift($nameArray));
                $name = str_replace("_", " ", implode(" ", $nameArray));
                $abbreviation = $date->format("T");
                $gmt = $date->format("P");

                if (!preg_match("/[A-Z]+/", $abbreviation)) {
                    $abbreviation = null;
                }

                return new Entity(
                    compact(
                        "id",
                        "name",
                        "region",
                        "timezone",
                        "abbreviation",
                        "gmt"
                    )
                );
            })
            // ->where('abbreviation', '!==', null)
            ->keyBy("id");
    }

    /**
     * Shorthand function to reduce previous code duplication.
     * @param $timezone
     * @param bool $groupView
     * @return string
     */
    public static function displayTimezoneName($timezone, $groupView = false)
    {
        $name =
            $timezone->region . ($timezone->name ? "/" . $timezone->name : "");

        $name .=
            " (" .
            implode(
                "",
                array_filter([$timezone->abbreviation, $timezone->gmt])
            ) .
            ")";

        return $name;
    }
}
