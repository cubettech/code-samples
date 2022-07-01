package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SignupResponseDto(

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("accessToken")
	val accessToken: String? = null

): Dto()

data class LocationsItem(

	@field:SerializedName("gymId")
	val gymId: GymId? = null,

	@field:SerializedName("locationName")
	val locationName: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)

data class GymId(

	@field:SerializedName("companyName")
	val companyName: String? = null,

	@field:SerializedName("_id")
	val id: String? = null
)

data class User(

	@field:SerializedName("locations")
	val locations: List<LocationsItem?>? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("loginMode")
	val loginMode: String? = null,

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("lastName")
	val lastName: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("active")
	val active: Boolean? = null,

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("dob")
	val dob: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("weight")
	val weight: String? = null,

	@field:SerializedName("height")
	val height: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("timeZone")
	val timeZone: String? = null,

	@field:SerializedName("locationId")
	val locationId: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null

)
