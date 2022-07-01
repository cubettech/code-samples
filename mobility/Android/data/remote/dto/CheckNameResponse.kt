package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CheckNameResponse(

	@field:SerializedName("available")
	val available: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)
