package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeaderBoardMenuResponse(

	@field:SerializedName("leaderBoards")
	val leaderBoards: MutableList<LeaderBoardsItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class LeaderBoardsItem(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("gymId")
	val gymId: String? = null,

	@field:SerializedName("endDate")
	val endDate: String? = null,

	@field:SerializedName("isDelete")
	val isDelete: Boolean? = null,

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("locationIds")
	val locationIds: List<String?>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("autoRenew")
	val autoRenew: Boolean? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("startDate")
	val startDate: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)
