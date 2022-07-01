package com.hrmfitclub.android.view.configuration

import android.content.Context
import androidx.lifecycle.liveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.hrmfitclub.android.data.ApiRepository
import com.hrmfitclub.android.data.remote.dto.LocationsItem
import com.hrmfitclub.android.data.remote.dto.SignupResponseDto
import com.hrmfitclub.android.misc.State
import com.hrmfitclub.android.misc.ext.get
import com.hrmfitclub.android.misc.ext.set
import com.hrmfitclub.android.utils.getErrorMessageFromGenericResponse
import com.hrmfitclub.android.view.AbstractViewModel
import com.hrmfitclub.android.view.ViewConstants
import retrofit2.HttpException
import timber.log.Timber

class ConfigurationViewModel(private val repository: ApiRepository, val context: Context, val gson: Gson) : AbstractViewModel() {

    fun configUser(userId: String,
                   height: String,
                   weight: String,
                   userName: String,
                   locationId: String,
                   country: String,
                   dob: String,
                   gender: String,
                   timeZone: String,
                   profilePicName: String?
    ) = liveData {
        emit(State.loading(data = null))
        try {
            //Request with a suspended repository function
            val data = repository.saveConfig(userId,
                height,
                weight,
                userName,
                locationId,
                country,
                dob,
                gender,
                timeZone,
                profilePicName)
            storeData(gson.toJson(data))
            emit(State.success(data = data))
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(t)
            if (t is HttpException) {
                val errorMessage =
                    getErrorMessageFromGenericResponse(
                        t
                    )
                if (errorMessage.isNullOrBlank()) {
                    emit(State.error(data = null, message = t.message ?: "Error Occurred!"))
                } else {
                    emit(State.error(data = null, message = errorMessage))
                }
            } else {
                emit(State.error(data = null, message = t.message ?: "Error Occurred!"))
            }
        }
    }

    fun checkUserName(name: String) = liveData {
        emit(State.loading(data = null))
        try {
            emit(State.success(data = repository.checkUserName(name)))
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage =
                    getErrorMessageFromGenericResponse(
                        t
                    )
                if (errorMessage.isNullOrBlank()) {
                    emit(State.error(data = null, message = t.message ?: "Error Occurred!"))
                } else {
                    emit(State.error(data = null, message = errorMessage))
                }
            } else {
                emit(State.error(data = null, message = t.message ?: "Error Occurred!"))
            }
        }
    }


    fun getLocationsList(): List<LocationsItem?>? {
        val pref = context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        val data = pref.get(ViewConstants.PREF_USER_SIGNUP, "")

        val response = gson.fromJson(data, SignupResponseDto::class.java)
        return response.user?.locations

        Timber.d("Data stored: ${pref.get(ViewConstants.PREF_USER_SIGNUP, "")}")
    }

    fun getUserId(): String? {
        val pref = context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        val data = pref.get(ViewConstants.PREF_USER_SIGNUP, "")

        val response = gson.fromJson(data, SignupResponseDto::class.java)
        return response.user?.userId
    }

    private fun storeData(value: String) {
        val pref = context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        pref.set(ViewConstants.PREF_USER_SIGNUP, value)
        Timber.d("Data stored: ${pref.get(ViewConstants.PREF_USER_SIGNUP, "")}")
    }

}