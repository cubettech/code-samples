package com.hrmfitclub.android.data.remote.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AllBoardUsersResponse(

	@field:SerializedName("currentUser")
	val currentUser: CurrentUser? = null,

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("userPosition")
	val userPosition: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("users")
	val users: MutableList<UsersItem?>? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
) : Parcelable

@Parcelize
data class CurrentUser(

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

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("loginMode")
	val loginMode: String? = null,

	@field:SerializedName("points")
	val points: Int? = null,

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
) : Parcelable

@Parcelize
data class UsersItem(

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

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("userName")
	var userName: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("loginMode")
	val loginMode: String? = null,

	@field:SerializedName("points")
	val points: Int? = null,

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("dob")
	val dob: String? = null,

	@field:SerializedName("locationId")
	val locationId: String? = null,

	@field:SerializedName("rank")
	val rank: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("height")
	val height: String? = null,

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null

) : Parcelable
