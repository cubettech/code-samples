package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AllFeedsResponse(

	@field:SerializedName("report")
	val report: List<ReportItem?>? = null,

	@field:SerializedName("feeds")
	val feeds: List<FeedsItem?>? = null,

	@field:SerializedName("block")
	val block: List<BlockItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("count")
	val count: Int? = null
)

data class ReportItem(

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class UserId(

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)

data class FeedsItem(

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
	val activityId: ActivityId? = null,

	@field:SerializedName("bluePoints")
	val bluePoints: Int? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("locationId")
	val locationId: LocationId? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("redPointsTime")
	val redPointsTime: String? = null,

	@field:SerializedName("greenPointsTime")
	val greenPointsTime: String? = null,

	@field:SerializedName("ownLikeStatus")
	var ownLikeStatus: Boolean? = null,

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
	val userId: UserId? = null,

	@field:SerializedName("picture")
	val picture: String? = null,

	@field:SerializedName("imoji")
	val imoji: Int? = null,

	@field:SerializedName("avgHr")
	val avgHr: String? = null,

	@field:SerializedName("yellowPoints")
	val yellowPoints: Int? = null,

	@field:SerializedName("totalComments")
	var totalComments: Int? = null,

	@field:SerializedName("comment")
	val comment: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("totalLikes")
	var totalLikes: Int? = null,

	@field:SerializedName("likedUsers")
	val likedUsers: List<String?>? = null,

	@field:SerializedName("commendedUsers")
	val commendedUsers: List<String?>? = null
)

data class LocationId(

	@field:SerializedName("locationName")
	val locationName: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)

data class ActivityId(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)

data class BlockItem(

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)
