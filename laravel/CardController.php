<?php

namespace App\Http\Controllers\Api;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Config, Auth, DB, Log;
use App\Models\Api\User;
use Carbon\Carbon;
use App\Models\Api\Reaction;

class CardController extends Controller
{
    /**
    * @SWG\Get(
    *   path="/card/get-cards",
    *   tags={"Cards"},
    *   summary="Gets the cards list wrt the search conditions set from the app search screen",
    *   @SWG\Parameter(
    *     name="Authorization",
    *     in="header",
    *     description="token format: Bearer \<JWTTOKEN\>",
    *     required=true,
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="page",
    *     in="query",
    *     description="page number for pagination",
    *     type="number",
    *   ),
    *   @SWG\Parameter(
    *     name="user_type",
    *     in="query",
    *     description="User type filter, for both users leave this filter blank.",
    *     enum={"user", "trainer", "both"},
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="gender",
    *     in="query",
    *     description="Gender filter, for both gender leave this filter blank",
    *     enum={"male", "female", "both"},
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="age_from",
    *     in="query",
    *     description="Age From Filter",
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="age_to",
    *     in="query",
    *     description="Age To Filter",
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="radius",
    *     in="query",
    *     description="Radius in KM to search for nearby cards",
    *     type="number",
    *   ),
    *   @SWG\Parameter(
    *     name="lat",
    *     in="query",
    *     description="Users location latitude",
    *     type="number",
    *   ),
    *   @SWG\Parameter(
    *     name="lng",
    *     in="query",
    *     description="Users location longituce",
    *     type="number",
    *   ),
    *   @SWG\Parameter(
    *     name="expertise",
    *     in="query",
    *     description="Array of all expertise like [2, 3, 5]",
    *     type="number",
    *   ),
    *   @SWG\Parameter(
    *     name="save_settings",
    *     in="query",
    *     description="set this value to 1 for the first request made after saving the filters. System will save the required filters and updates user personal infos etc.",
    *     type="number",
    *   ),
    *
    *   @SWG\Response(response=200, description="successful operation"),
    *   @SWG\Response(response=400, description="Validation"),
    *   @SWG\Response(response=401, description="Unauthorised"),
    * )
    */
    public function getCards(Request $request)
    {
        try{
            $user = Auth::user();
            $perPage = 10;
            $cards = User::with(['homeGym'])
                        ->withCount(['isLiked'])
                        ->where('status', Config::get('sSweat.user_status.active'))
                        ->where('users.id', '!=', $user->id)
                        ->whereDoesntHave(
                            'receivedReactions', function ($query) use ($user) {
                                        $query->where('user_id', $user->id);
                            }
                        );
            /**
             * User Type filter
             */
            if(isset($request->user_type) && $request->user_type != "" && $request->user_type != "both") {
                  $cards->where('user_type', Config::get("sSweat.user_type.{$request->user_type}"));
            }
            /**
             * Gender Filter
             */
            if(isset($request->gender) && $request->gender != "" && $request->gender != "both") {
                  $cards->whereHas(
                      'profile', function ($query) use ($request) {
                        $query->where('gender', Config::get("sSweat.gender.{$request->gender}"));
                      }
                  );
            }
            /**
             * Age Filter
             */
            if(isset($request->age_from) && $request->age_from != "" && isset($request->age_to) && $request->age_to != "") {
                  $now = Carbon::now();
                  $start = $now->subYears($request->age_from - 1)->toDateString();
                  $now = Carbon::now();
                  $end = $now->subYears($request->age_to + 1)->subDay()->toDateString(); // plus 1 year minus a day
                  $cards->whereHas(
                      'profile', function ($query) use ($start, $end) {
                        $query->whereBetween('dob', [$end, $start]);
                      }
                  );
            }

            if($request->radius && $request->lat && $request->lng) {
                  $cards = $this->__haversine($cards, $request->lat, $request->lng, $request->radius, 'kilometers');
            }else{
              $cards = $cards
                  ->inRandomOrder()
                  ->paginate($perPage);
            }

            return response()->json(
                [
                'status' => 1,
                'data' => $cards
                ], 200
            );
        }catch(\Excepton $e){
            return response()->json(
                [
                'status' => 0,
                //'message' => Config::get('sSweatLang.api.something_went_wrong')
                'message' => $e->getMessage()
                ], 400
            );
        }
    }

    /**
    * @SWG\Post(
    *   path="/card/react",
    *   tags={"Cards"},
    *   summary="Handles the left/right Swipes from the App with like/ignore",
    *   @SWG\Parameter(
    *     name="Authorization",
    *     in="header",
    *     description="token format: Bearer \<JWTTOKEN\>",
    *     required=true,
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="card_id",
    *     in="formData",
    *     description="ID of the swiped Card",
    *     required=true,
    *     type="string",
    *   ),
    *   @SWG\Parameter(
    *     name="reaction",
    *     in="formData",
    *     description="User reaction to this card",
    *     enum={"like", "ignore"},
    *     required=true,
    *     type="string",
    *   ),
    *   @SWG\Response(response=200, description="successful operation"),
    *   @SWG\Response(response=400, description="Validation"),
    *   @SWG\Response(response=401, description="Unauthorised"),
    * )
    */
    public function react(Request $request)
    {
        try{
            $user = Auth::user();
            $reaction = $request->reaction;
            $cardId = $request->card_id;

            $reactionEntry = Reaction::updateOrCreate(
                ['card_id' => $cardId, 'user_id' => $user->id],
                ['reaction' => Config::get("sSweat.reaction.{$reaction}")]
            );
            Log::info("PROCESSING_REACTION:: " . $user->id . "::" . $cardId);
              return response()->json(
                  [
                  'status' => 1,
                  ], 200
              );

        }catch(\Exception $e){
            return response()->json(
                [
                'status' => 0,
                'message' => $e->getMessage()
                ], 400
            );
        }
    }

    /**
    * @SWG\Post(
    *   path="/card/reset-cards",
    *   tags={"Cards"},
    *   summary="Resets the swipes, removes all the left swipe from the DB",
    *   @SWG\Parameter(
    *     name="Authorization",
    *     in="header",
    *     description="token format: Bearer \<JWTTOKEN\>",
    *     required=true,
    *     type="string",
    *   ),
    *   @SWG\Response(response=200, description="successful operation"),
    *   @SWG\Response(response=400, description="Validation"),
    *   @SWG\Response(response=401, description="Unauthorised"),
    * )
    */
    public function resetCards(Request $request){
      try{
        $user = Auth::user();
        Reaction::where('user_id', $user->id)
              ->where('reaction', Config::get("sSweat.reaction.ignore"))->delete();
        return response()->json(
            [
            'status' => 1,
            ], 200
        );
      }catch(\Excepton $e){
        return response()->json(
            [
            'status' => 0,
            'message' => $e->getMessage()
            ], 400
        );
      }
    }

    public static function __haversine($query, $lat, $lng, $max_distance = 25, $units = 'miles', $fields = false )
    {

        if(empty($lat)) {
            $lat = 0;
        }
        if(empty($lng)) {
            $lng = 0;
        }
        /*
         *  Allow for changing of units of measurement
         */
        switch ( $units ) {
        case 'miles':
            //radius of the great circle in miles
            $gr_circle_radius = 3959;
            break;
        case 'kilometers':
            //radius of the great circle in kilometers
            $gr_circle_radius = 6371;
            break;
        }
        /*
         *  Support the selection of certain fields
         */
        if(! $fields ) {
            $fields = array( 'users.*' );
        }
        /*
         *  Generate the select field for disctance
         */
        $distance_select = sprintf("(ROUND(( %d * acos( cos( radians(%s) ) " .
                                            " * cos( radians( user_addresses.lat ) ) " .
                                            " * cos( radians( user_addresses.long ) - radians(%s) ) " .
                                            " + sin( radians(%s) ) * sin( radians( user_addresses.lat ) ))), 2) )",
            $gr_circle_radius,
            $lat,
            $lng,
            $lat
        );

        $result = $query->join('user_addresses', 'users.id', '=', 'user_addresses.user_id')
            ->selectRaw("$distance_select AS distance")
            ->whereRaw("$distance_select <= $max_distance")
            //->orderBy("$distance_select AS distance", 'ASC')
            ->inRandomOrder()
            ->paginate(15);
        return $result;
    }
}
