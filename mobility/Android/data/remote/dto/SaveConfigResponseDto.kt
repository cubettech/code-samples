package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SaveConfigResponseDto(

	@field:SerializedName("accessToken")
	val accessToken: String? = null,

	@field:SerializedName("user")
	val user: ConfigUser? = null
) :  Dto()

data class ConfigUser(

	@field:SerializedName("lastName")
	val lastName: String? = null,

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("gender")
	val gender: Int? = null,

	@field:SerializedName("weight")
	val weight: String? = null,

	@field:SerializedName("timeZone")
	val timeZone: String? = null,

	@field:SerializedName("active")
	val active: Boolean? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("loginMode")
	val loginMode: String? = null,

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("dob")
	val dob: String? = null,

	@field:SerializedName("locationId")
	val locationId: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("height")
	val height: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null
)
