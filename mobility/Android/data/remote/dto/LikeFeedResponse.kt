package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LikeFeedResponse(

	@field:SerializedName("feed")
	val feed: FeedsItem? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)