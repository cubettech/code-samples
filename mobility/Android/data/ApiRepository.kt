package com.hrmfitclub.android.data

import com.google.gson.JsonObject
import com.hrmfitclub.android.data.remote.dto.*
import com.hrmfitclub.android.data.remote.endpoint.HRMWebService
import retrofit2.http.Field

class ApiRepository(private val remoteDataSource: HRMWebService) {

    suspend fun getData(query: String): SampleResponseDto {
        //remote data source Request
        return remoteDataSource.getData(query, "Type1")
    }

    suspend fun signUp(
        firstName: String, lastName: String, email: String, password: String,
        lat: String, lon: String, mode: String
    ): SignupResponseDto {
        val jsonObject = JsonObject()
        jsonObject.addProperty("firstName", firstName)
        if (lastName.trim().isNotEmpty()) {
            jsonObject.addProperty("lastName", lastName)
        }
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("password", password)
        jsonObject.addProperty("lat", lat.toDouble())
        jsonObject.addProperty("lon", lon.toDouble())
        jsonObject.addProperty("loginMode", mode)
        return remoteDataSource.signup(jsonObject)
    }

    suspend fun signUpSocial(
        firstName: String, email: String, lat: String, lon: String, mode: String): SignupResponseDto {
        val jsonObject = JsonObject()
        jsonObject.addProperty("firstName", firstName)
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lon", lon)
        jsonObject.addProperty("loginMode", mode)
        return remoteDataSource.signupSocial(jsonObject)
    }

    suspend fun saveConfig(
        userId: String,
        height: String,
        weight: String,
        userName: String,
        locationId: String,
        country: String,
        dob: String,
        gender: String,
        timeZone: String,
        profilePicName : String?
    ): SaveConfigResponseDto {
        val jsonObject = JsonObject()
        jsonObject.addProperty("userId", userId)
        jsonObject.addProperty("height", height)
        jsonObject.addProperty("weight", weight)
        jsonObject.addProperty("userName", userName)
        jsonObject.addProperty("locationId", locationId)
        jsonObject.addProperty("country", country)
        jsonObject.addProperty("dob", dob)
        jsonObject.addProperty("gender", gender)
        jsonObject.addProperty("timeZone", timeZone)
        if (profilePicName != null && profilePicName.isNotEmpty()) {
            jsonObject.addProperty("profilePicture", profilePicName)
        }
        return remoteDataSource.saveConfig(jsonObject)
    }

    suspend fun signIn(email: String, password: String): SignupResponseDto {
        val jsonObject = JsonObject()
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("password", password)
        return remoteDataSource.signIn(jsonObject)
    }

    suspend fun sendOtp(email: String): Dto {
        return remoteDataSource.sendOtp(email)
    }

    suspend fun resetPassword(email: String, otp: String, password: String): Dto {
        return remoteDataSource.resetPassword(email, otp, password)
    }

    suspend fun checkEmail(email: String): SignupResponseDto {
        return remoteDataSource.checkEmail(email)
    }

    suspend fun checkUserName(name: String): CheckNameResponse {
        return remoteDataSource.checkUserName(name)
    }

    suspend fun getAllActivities(): ActivitiesResponseDto {
        return remoteDataSource.getActivities()
    }

    suspend fun getUserActivities(userId: String): ActivitiesResponseDto {
        return remoteDataSource.getUserActivities(userId)
    }

    suspend fun postWorkoutData(json: JsonObject, token: String): PostFeedResponse {
        return remoteDataSource.postWorkoutData( "Bearer $token", json)
    }

    suspend fun postWorkoutDataToFeed(json: JsonObject, token: String): PostFeedResponse {
        return remoteDataSource.postWorkoutDataToFeed( "Bearer $token", json)
    }

    suspend fun listUserBoards(userId: String?, token: String): LeaderBoardMenuResponse {
        val json = JsonObject()
        json.addProperty("userId", userId)
        return remoteDataSource.listUserBoards( "Bearer $token", json)
    }

    suspend fun getAllBoardUsers(userId: String?, boardId: String?, type: String,
                                 limit: Int, skip: Int, token: String): AllBoardUsersResponse {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("boardId", boardId)
        json.addProperty("type", type)
        json.addProperty("limit", limit)
        json.addProperty("skip", skip)
        return remoteDataSource.listAllBoardUsers( "Bearer $token", json)
    }

    suspend fun getWorkoutHistory(userId: String?, limit: Int, skip: Int, token: String): WorkoutHistoryResponse {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("limit", limit)
        json.addProperty("skip", skip)
        return remoteDataSource.getActivityHistory( "Bearer $token", json)
    }

    suspend fun getFeeds(userId: String?, limit: Int, skip: Int, token: String): AllFeedsResponse {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("limit", limit)
        json.addProperty("skip", skip)
        return remoteDataSource.getAllFeeds("Bearer $token", json)
    }

    suspend fun getLikedUsers(feedId: String?, limit: Int, skip: Int, token: String): LikedUsersResponse {
        val json = JsonObject()
        json.addProperty("feedId", feedId)
        json.addProperty("limit", limit)
        json.addProperty("skip", skip)
        return remoteDataSource.getLikes("Bearer $token", json)
    }

    suspend fun getComments(feedId: String?, limit: Int, skip: Int, token: String): CommentsResponse {
        val json = JsonObject()
        json.addProperty("feedId", feedId)
        json.addProperty("limit", limit)
        json.addProperty("skip", skip)
        return remoteDataSource.getComments("Bearer $token", json)
    }

    suspend fun sendComment(userId: String?, feedId: String?, comment: String, token: String): Dto {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("feedId", feedId)
        json.addProperty("commentText", comment)
        return remoteDataSource.sendComment("Bearer $token", json)
    }

    suspend fun likeFeed(userId: String?, feedId: String?, token: String): LikeFeedResponse {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("feedId", feedId)
        return remoteDataSource.likeFeed("Bearer $token", json)
    }

    suspend fun reportFeed(userId: String?, feedId: String?, title: String?, comment: String?, token: String): Dto {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("feedId", feedId)
        json.addProperty("title", title)
        if (comment?.trim()?.isNotEmpty()!!) {
            json.addProperty("comment", comment)
        }
        return remoteDataSource.reportFeed("Bearer $token", json)
    }

    suspend fun blockFeed(userId: String?, feedId: String?, title: String?, comment: String, token: String): Dto {
        val json = JsonObject()
        json.addProperty("userId", userId)
        json.addProperty("feedId", feedId)
        json.addProperty("title", title)
        if (comment.trim().isNotEmpty()) {
            json.addProperty("comment", comment)
        }
        return remoteDataSource.blockFeed("Bearer $token", json)
    }

}