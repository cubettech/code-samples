package com.hrmfitclub.android.view.login

import android.content.Context
import androidx.lifecycle.liveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.hrmfitclub.android.data.ApiRepository
import com.hrmfitclub.android.data.remote.dto.SignupResponseDto
import com.hrmfitclub.android.misc.State
import com.hrmfitclub.android.misc.ext.get
import com.hrmfitclub.android.misc.ext.set
import com.hrmfitclub.android.utils.getErrorMessageFromGenericResponse
import com.hrmfitclub.android.view.AbstractViewModel
import com.hrmfitclub.android.view.ViewConstants
import retrofit2.HttpException
import timber.log.Timber

class LoginViewModel(private val repository: ApiRepository, val gson: Gson, val context: Context) : AbstractViewModel() {

    fun signIn(email: String, password: String) = liveData {
        emit(State.loading(data = null))
        try {
            val data = repository.signIn(email, password)
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

    fun sendOtp(email: String) = liveData {
        emit(State.loading(data = null))
        try {
            emit(State.success(data = repository.sendOtp(email)))
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

    fun resetPassword(email: String, otp: String, newPassword: String) = liveData {
        emit(State.loading(data = null))
        try {
            emit(State.success(data = repository.resetPassword(email, otp, newPassword)))
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

    fun checkEmail(email: String) = liveData {
        emit(State.loading(data = null))
        try {
            val data = repository.checkEmail(email)
            storeData(gson.toJson(data))
            emit(State.success(data = data))
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

    fun getRegisteredEmail(): String? {
        var userData: SignupResponseDto? = null
        val pref = context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        val data = pref.get(ViewConstants.PREF_USER_SIGNUP, "")
        Timber.d("Data stored: $data")
        if (data!!.isNotEmpty()) {
            userData = gson.fromJson(data, SignupResponseDto::class.java)
        }
        return userData?.user?.email
    }
}