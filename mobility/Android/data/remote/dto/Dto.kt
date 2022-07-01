package com.hrmfitclub.android.data.remote.dto

import com.google.gson.annotations.SerializedName

open class Dto(
        @SerializedName("status")
        var status: Boolean? = null,

        @SerializedName("message")
        var message: String? = null,

        @SerializedName("error")
        var error: String? = null
)