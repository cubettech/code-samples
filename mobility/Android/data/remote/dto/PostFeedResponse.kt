package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostFeedResponse(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class Result(

	@field:SerializedName("maxHR")
	val maxHR: String? = null,

	@field:SerializedName("redPoints")
	val redPoints: Int? = null,

	@field:SerializedName("workoutTime")
	val workoutTime: String? = null,

	@field:SerializedName("greenPoints")
	val greenPoints: Int? = null,

	@field:SerializedName("orangePointsTime")
	val orangePointsTime: String? = null,

	@field:SerializedName("points")
	val points: Int? = null,

	@field:SerializedName("activityId")
	val activityId: String? = null,

	@field:SerializedName("bluePoints")
	val bluePoints: Int? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("locationId")
	val locationId: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("redPointsTime")
	val redPointsTime: String? = null,

	@field:SerializedName("greenPointsTime")
	val greenPointsTime: String? = null,

	@field:SerializedName("yellowPointsTime")
	val yellowPointsTime: String? = null,

	@field:SerializedName("bluePointsTime")
	val bluePointsTime: String? = null,

	@field:SerializedName("totalTime")
	val totalTime: String? = null,

	@field:SerializedName("orangePoints")
	val orangePoints: Int? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("picture")
	val picture: String? = null,

	@field:SerializedName("imoji")
	val imoji: Int? = null,

	@field:SerializedName("avgHr")
	val avgHr: String? = null,

	@field:SerializedName("yellowPoints")
	val yellowPoints: Int? = null,

	@field:SerializedName("comment")
	val comment: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)
