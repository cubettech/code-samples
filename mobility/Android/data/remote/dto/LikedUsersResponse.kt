package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LikedUsersResponse(

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("likes")
	val likes: List<LikesItem?>? = null
)

data class LikesItem(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("feedId")
	val feedId: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("userId")
	val userId: LikedUserId? = null
)

data class LikedUserId(

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)
