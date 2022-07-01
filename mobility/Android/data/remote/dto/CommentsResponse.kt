package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CommentsResponse(

	@field:SerializedName("comments")
	val comments: List<CommentsItem?>? = null,

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class CommentsItem(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("feedId")
	val feedId: String? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("userId")
	val userId: CommentUserId? = null,

	@field:SerializedName("commentText")
	val commentText: String? = null
)

data class CommentUserId(

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)
