package com.hrmfitclub.android.view.signup

import android.content.Context
import androidx.lifecycle.liveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.hrmfitclub.android.data.ApiRepository
import com.hrmfitclub.android.misc.State
import com.hrmfitclub.android.misc.ext.get
import com.hrmfitclub.android.misc.ext.set
import com.hrmfitclub.android.utils.getErrorMessageFromGenericResponse
import com.hrmfitclub.android.view.AbstractViewModel
import com.hrmfitclub.android.view.ViewConstants
import retrofit2.HttpException
import timber.log.Timber

class SignupViewModel(private val repository: ApiRepository, val context: Context, val gson: Gson) : AbstractViewModel() {

    fun signupUser(firstName: String, lastname: String, email: String, password: String, lat: String, lon: String, mode: String) = liveData {
        emit(State.loading(data = null))
        try {
            //Request with a suspended repository function
            if (mode == "google" || mode == "facebook") {
                val data = repository.signUpSocial(firstName, email, lat, lon, mode)
                storeData(gson.toJson(data))
                emit(State.success(data = data))
            } else {
                val data = repository.signUp(firstName, lastname, email, password, lat, lon, mode)
                storeData(gson.toJson(data))
                emit(State.success(data = data))
            }
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

    fun checkEmail(email: String) = liveData {
        emit(State.loading(data = null))
        try {
            emit(State.success(data = repository.checkEmail(email)))
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

    private fun storeData(value: String) {
        val pref = context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        pref.set(ViewConstants.PREF_USER_SIGNUP, value)
        Timber.d("Data stored: ${pref.get(ViewConstants.PREF_USER_SIGNUP, "")}")
    }

}
