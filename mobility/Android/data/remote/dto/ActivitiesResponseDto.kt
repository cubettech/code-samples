package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActivitiesResponseDto(

	@field:SerializedName("activities")
	val activities: List<ActivitiesItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class ActivitiesItem(

	@field:SerializedName("__v")
	val V: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("idNumber")
	val idNumber: Int? = null
)
