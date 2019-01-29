<?php

namespace App\Http\Controllers\Api;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Auth, DB, Config, Storage;
use App\Models\Api\User;
use App\Models\Api\UserProfile;
use App\Models\Api\UserAddress;
use App\Models\Api\UserSocialAccounts;
use App\Models\Api\UserProfessionalInfo;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;
use Intervention\Image\Facades\Image as Image;

class ProfileController extends Controller
{
  /**
  * @SWG\Post(
  *   path="/profile/update-user-personal-info",
  *   tags={"Profile"},
  *   summary="Updates the registred users profile information.",
  *   operationId="getCustomerRates",
  *   @SWG\Parameter(
  *     name="Authorization",
  *     in="header",
  *     description="token format: Bearer \<JWTTOKEN\>",
  *     required=true,
  *     type="string",
  *   ),
  *   @SWG\Parameter(
  *     name="avatar",
  *     in="formData",
  *     description="Profile avatar image",
  *     required=true,
  *     type="file",
  *   ),
  *   @SWG\Parameter(
  *     name="dob",
  *     in="formData",
  *     description="User date of birth in YYYY-MM-DD format",
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="gender",
  *     in="formData",
  *     description="User gender (male , female)",
  *     enum={"male", "female"},
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="bio",
  *     in="formData",
  *     description="A short description about the user",
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Response(response=200, description="successful operation"),
  *   @SWG\Response(response=400, description="Validation"),
  *   @SWG\Response(response=401, description="Unauthorised"),
  * )
  */
  public function updateUserPersonalInfo(Request $request){
    try{
      $user = Auth::user();
      $validator  = Validator::make($request->all(), [
          'avatar' => 'required',
          'dob' => 'required',
          'gender' => 'required | in:male,female,other',
          'bio' => 'required'
      ]);
      if($validator->fails()) {
          return response()->json(
              [
              'status' => 0,
              'message' => implode(", ",$validator->errors()->all())
              ], 400
          );
      }

      $avatar = $request->file('avatar');
      $filename = $this->__processAvatar($avatar, $user->id);

      $userProfile = UserProfile::where('user_id', $user->id)->first();
      $userProfile->dob = $request->dob;
      $userProfile->gender = Config::get("sSweat.gender.{$request->gender}");;
      $userProfile->bio = $request->bio;
      $userProfile->avatar = $filename;
      $userProfile->save();
      $user = User::with('profile')->find($user->id);
      return response()->json(
          [
          'status' => 1,
          'message' => Config::get("sSweatLang.api.profile_updated"),
          'data' => [
            'user' => $user,
          ]], 200
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
  *   path="/profile/update-trainer-personal-info",
  *   tags={"Profile"},
  *   summary="Updates the trainers profile information.",
  *   operationId="getCustomerRates",
  *   @SWG\Parameter(
  *     name="Authorization",
  *     in="header",
  *     description="token format: Bearer \<JWTTOKEN\>",
  *     required=true,
  *     type="string",
  *   ),
  *   @SWG\Parameter(
  *     name="avatar",
  *     in="formData",
  *     description="Profile avatar image",
  *     required=true,
  *     type="file",
  *   ),
  *   @SWG\Parameter(
  *     name="dob",
  *     in="formData",
  *     description="User date of birth in YYYY-MM-DD format",
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="gender",
  *     in="formData",
  *     description="User gender (male , female)",
  *     enum={"male", "female"},
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="bio",
  *     in="formData",
  *     description="A short description about the user",
  *     required=true,
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="social_accounts",
  *     in="formData",
  *     description="Json encoded object with properties facebook, twitter, instagram, snapchat",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="addresses",
  *     in="formData",
  *     description="JSON encoded Array of address objects. Address objects should have properties as address, city, state, country, lat, long. Which are full address, city, state, country, latitude and longitude from google API",
  *     type="string"
  *   ),
  *   @SWG\Response(response=200, description="successful operation"),
  *   @SWG\Response(response=400, description="Validation"),
  *   @SWG\Response(response=401, description="Unauthorised"),
  * )
  */
  public function updateTrainerPersonalInfo(Request $request){
    try{
      $user = Auth::user();
      $validator  = Validator::make($request->all(), [
          'avatar' => ['required'],
          //Rule::dimensions()->maxWidth(1000)->maxHeight(500)->ratio(3 / 2)
          'dob' => 'required',
          'gender' => 'required | in:male,female',
          'bio' => 'required'
      ]);
      if($validator->fails()) {
          return response()->json(
              [
              'status' => 0,
              'message' => implode(", ",$validator->errors()->all())
              ], 400
          );
      }

      $avatar = $request->file('avatar');
      $filename = $this->__processAvatar($avatar, $user->id);

      DB::beginTransaction();
      $userProfile = UserProfile::where('user_id', $user->id)->first();
      $userProfile->dob = $request->dob;
      $userProfile->gender = Config::get("sSweat.gender.{$request->gender}");
      $userProfile->bio = $request->bio;
      $userProfile->avatar = $filename;
      $userProfile->save();

      $addresses = json_decode($request->addresses);
      if($addresses){
        foreach($addresses as $address){
          $userAddress = new UserAddress();
          $userAddress->user_id = $user->id;
          $userAddress->address = $address->address;
          $userAddress->city = $address->city;
          $userAddress->state = $address->state;
          $userAddress->country = $address->country;
          $userAddress->lat = $address->lat;
          $userAddress->long = $address->long;
          $userAddress->save();
        }
      }

      $socialAccounts = json_decode($request->social_accounts);
      if($socialAccounts){
        $userSocialAccount = UserSocialAccounts::updateOrCreate(
                      ['user_id' => $user->id],
                      ['facebook' => $socialAccounts->facebook, 'twitter' => $socialAccounts->twitter, 'instagram' => $socialAccounts->instagram, 'snapchat' => $socialAccounts->snapchat]
                  );
      }
      DB::commit();

      $user->profile;
      return response()->json(
          [
          'status' => 1,
          'message' => Config::get("sSweatLang.api.profile_updated"),
          'data' => [
            'user' => $user,
          ]], 200
      );
    }catch(\Exception $e){
      DB::rollback();
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
  *   path="/profile/update-trainer-professional-info",
  *   tags={"Profile"},
  *   summary="Updates the trainers professional information.",
  *   operationId="getCustomerRates",
  *   @SWG\Parameter(
  *     name="Authorization",
  *     in="header",
  *     description="token format: Bearer \<JWTTOKEN\>",
  *     required=true,
  *     type="string",
  *   ),
  *   @SWG\Parameter(
  *     name="qualifications",
  *     in="formData",
  *     description="Selected qualifications as a json array eg ['q1', 'q2', 'q3']",
  *     type="string",
  *   ),
  *   @SWG\Parameter(
  *     name="training_techniques",
  *     in="formData",
  *     description="Selected training techniques as a json string eg ['tt1', 'tt3']",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="health_conditions",
  *     in="formData",
  *     description="Selected health conditions as a json string eg ['hc3']",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="population",
  *     in="formData",
  *     description="Selected population as a json string eg ['pop3']",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="certificates",
  *     in="formData",
  *     description="Selected certificates as a json string eg ['cer3']",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="insurance",
  *     in="formData",
  *     description="Selected insurance as a json string eg ['ins2']",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="fa_reg_no",
  *     in="formData",
  *     description="Fitness Australia Registration Number",
  *     type="string"
  *   ),
  *   @SWG\Parameter(
  *     name="experience",
  *     in="formData",
  *     description="Experience in years",
  *     type="string"
  *   ),
  *   @SWG\Response(response=200, description="successful operation"),
  *   @SWG\Response(response=400, description="Validation"),
  *   @SWG\Response(response=401, description="Unauthorised"),
  * )
  */
  public function updateTrainerProfessionalInfo(Request $request){
    try{
      $user = Auth::user();
      $validator  = Validator::make($request->all(), [
          'qualifications' => 'required',
          'training_techniques' => 'required',
          'health_conditions' => 'required',
          'population' => 'required',
          'certificates' => 'required',
          'insurance' => 'required',
          'fa_reg_no' => 'required',
          'experience' => 'required'
      ]);
      if($validator->fails()) {
          return response()->json(
              [
              'status' => 0,
              'message' => implode(", ",$validator->errors()->all())
              ], 400
          );
      }
      $qulalificationArray = [];
      $ttArray = [];
      $hcArray = [];
      $popArray = [];
      $cerArray = [];
      $insArray = [];
      if($request->qualifications){
        $qualificationInput = json_decode($request->qualifications);
        foreach($qualificationInput as $qualificationKey){
          $qulalificationArray[] = Config::get("sSweat.qualifications.{$qualificationKey}");
        }
      }
      if($request->training_techniques){
        $ttInput = json_decode($request->training_techniques);
        foreach($ttInput as $ttKey){
          $ttArray[] = Config::get("sSweat.training_techniques.{$ttKey}");
        }
      }
      if($request->health_conditions){
        $hcInput = json_decode($request->health_conditions);
        foreach($hcInput as $hcKey){
          $hcArray[] = Config::get("sSweat.health_conditions.{$hcKey}");
        }
      }
      if($request->population){
        $popInput = json_decode($request->population);
        foreach($popInput as $popKey){
          $popArray[] = Config::get("sSweat.population.{$popKey}");
        }
      }
      if($request->certificates){
        $cerInput = json_decode($request->certificates);
        foreach($cerInput as $cerKey){
          $cerArray[] = Config::get("sSweat.certificates.{$cerKey}");
        }
      }
      if($request->insurance){
        $insInput = json_decode($request->insurance);
        foreach($insInput as $insKey){
          $insArray[] = Config::get("sSweat.insurance.{$insKey}");
        }
      }
      $userProfessioanlInfo = UserProfessionalInfo::updateOrCreate(
        ['user_id' => $user->id],
        [
          'qualifications' => json_encode($qulalificationArray),
          'training_techniques' => json_encode($ttArray),
          'health_conditions' => json_encode($hcArray),
          'population' => json_encode($popArray),
          'certificates' => json_encode($cerArray),
          'insurance' => json_encode($insArray),
          'fa_reg_no' => $request->fa_reg_no,
          'experience' => $request->experience
        ]
      );

      return response()->json(
          [
          'status' => 1,
          'message' => Config::get("sSweatLang.api.profile_updated"),
          'data' => [
            'user' => $user,
          ]], 200
      );


    }catch(\Exception $e){
      //DB::rollback();
      return response()->json(
          [
          'status' => 0,
          'message' => $e->getMessage()
          ], 400
      );
    }
  }
/**
  * @SWG\Get(
  *   path="/profile/trainer-professional-info-options",
  *   tags={"Profile"},
  *   summary="Updates the registred users profile information.",
  *   operationId="getCustomerRates",
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
  public function getTrainerCreateOptions(){
      try{
          return response()->json(
              [
                  'qualifications' =>  Config::get("sSweat.qualifications_ui"),
                  'training_techniques' => Config::get("sSweat.training_techniques_ui"),
                  'health_conditions' => Config::get("sSweat.health_conditions_ui"),
                  'population' => Config::get("sSweat.population_ui"),
                  'certificates' => Config::get("sSweat.certificates_ui"),
                  'insurance' => Config::get("sSweat.insurance_ui")
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

  private function __processAvatar($avatar, $userId){
    $destinationPath = storage_path('app/public/thumbnail/');
    $path = $avatar->store('images/'.$userId);
    $thumbImg = Image::make($avatar->getRealPath())->resize(100, 100);
    $thumbImg = $thumbImg->stream();
    Storage::put('thumbnail/'.$path, $thumbImg->__toString());
    return $path;
  }
}
