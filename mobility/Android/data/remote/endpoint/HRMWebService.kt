package com.hrmfitclub.android.data.remote.endpoint

import com.google.gson.JsonObject
import com.hrmfitclub.android.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface HRMWebService {

    @GET("query")
    suspend fun getData(@Query("s") query: String, @Query("type") type: String): SampleResponseDto

    @POST("user/app-register")
    suspend fun signup(
        @Body json: JsonObject
    ): SignupResponseDto

    @POST("user/app-register")
    suspend fun signupSocial(
        @Body json: JsonObject
    ): SignupResponseDto

    @POST("user/app-login")
    suspend fun signIn(
        @Body json: JsonObject
    ): SignupResponseDto

    @POST("user/save-config")
    suspend fun saveConfig(
        @Body json: JsonObject
    ): SaveConfigResponseDto

    @FormUrlEncoded
    @POST("user/app-forgot-password")
    suspend fun sendOtp(
        @Field("email") email: String
    ): Dto

    @FormUrlEncoded
    @POST("user/app-reset-password")
    suspend fun resetPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("password") password: String
    ): Dto

    @FormUrlEncoded
    @POST("user/check-email")
    suspend fun checkEmail(
        @Field("email") email: String): SignupResponseDto

    @FormUrlEncoded
    @POST("user/check-username")
    suspend fun checkUserName(
        @Field("userName") userName: String): CheckNameResponse

    @Multipart
    @POST("utils/image-upload")
    suspend fun imageUpload(
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part profilePicture: MultipartBody.Part): Dto

    @GET("activity/get-activity")
    suspend fun getActivities(): ActivitiesResponseDto

    @GET("activity/get-activity")
    suspend fun getUserActivities(@Query("userId") userId: String): ActivitiesResponseDto

    @POST("activity/create-activity-data")
    suspend fun postWorkoutData(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): PostFeedResponse

    @POST("activity/save-feed")
    suspend fun postWorkoutDataToFeed(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): PostFeedResponse

    @POST("board/list-user-boards")
    suspend fun listUserBoards(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): LeaderBoardMenuResponse

    @POST("board/app-board-users")
    suspend fun listAllBoardUsers(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): AllBoardUsersResponse

    @POST("activity/get-activity-history")
    suspend fun getActivityHistory(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): WorkoutHistoryResponse

    @POST("activity/get-all-feeds")
    suspend fun getAllFeeds(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): AllFeedsResponse

    @POST("activity/get-likes")
    suspend fun getLikes(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): LikedUsersResponse

    @POST("activity/get-comments")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): CommentsResponse

    @POST("activity/comment-post")
    suspend fun sendComment(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): Dto

    @POST("activity/like-post")
    suspend fun likeFeed(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): LikeFeedResponse

    @POST("activity/report-feed")
    suspend fun reportFeed(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): Dto

    @POST("activity/block-feed")
    suspend fun blockFeed(
        @Header("Authorization") token: String,
        @Body json: JsonObject
    ): Dto
}