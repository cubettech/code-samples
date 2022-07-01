package com.hrmfitclub.android.view.home

import android.content.Context
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hrmfitclub.android.data.ApiRepository
import com.hrmfitclub.android.data.remote.dto.ActivitiesItem
import com.hrmfitclub.android.data.remote.dto.ActivitiesResponseDto
import com.hrmfitclub.android.data.remote.dto.SignupResponseDto
import com.hrmfitclub.android.data.remote.dto.User
import com.hrmfitclub.android.misc.State
import com.hrmfitclub.android.misc.ext.get
import com.hrmfitclub.android.utils.getErrorMessageFromGenericResponse
import com.hrmfitclub.android.view.AbstractViewModel
import com.hrmfitclub.android.view.ViewConstants
import retrofit2.HttpException

class HomeViewModel(private val repository: ApiRepository, val context: Context, val gson: Gson) : AbstractViewModel() {

    private val pref by lazy {
        context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
    }

    fun getUserData(): User? {
        val data = pref.get(ViewConstants.PREF_USER_SIGNUP, "")
        if (data?.isNotEmpty()!!) {
            val response = gson.fromJson(data, SignupResponseDto::class.java)
            return response.user
        }
        return null
    }

    fun getUserToken(): String {
        val data = pref.get(ViewConstants.PREF_USER_SIGNUP, "")
        if (data?.isNotEmpty()!!) {
            val response = gson.fromJson(data, SignupResponseDto::class.java)
            return response.accessToken?: ""
        }
        return ""
    }

    fun getUserActivities(): List<ActivitiesItem?>? {
        val data = pref.get(ViewConstants.PREF_USER_ACTIVITIES, "")
        return if (data?.isNotEmpty()!!) {
            val activitiesDto = gson.fromJson(data, ActivitiesResponseDto::class.java)
            activitiesDto.activities
        } else {
            mutableListOf()
        }
    }

    fun getActivities(): List<ActivitiesItem?>? {
        val data = pref.get(ViewConstants.PREF_ACTIVITIES, "")
        return if (data?.isNotEmpty()!!) {
            val activitiesDto = gson.fromJson(data, ActivitiesResponseDto::class.java)
            activitiesDto.activities
        } else {
            mutableListOf()
        }
    }

    fun sendWorkoutData(json: JsonObject) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.postWorkoutData(json, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
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

    fun sendWorkoutDataToFeed(json: JsonObject) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.postWorkoutDataToFeed(json, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
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

    fun listUserBoards() = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.listUserBoards(user?.userId, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun getAllBoardUsers(boardId: String?, type: String, limit: Int, skip: Int) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.getAllBoardUsers(user?.userId, boardId, type, limit, skip, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun getWorkoutHistory(limit: Int, skip: Int) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.getWorkoutHistory(user?.userId, limit, skip, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun getFeeds(limit: Int, skip: Int) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.getFeeds(user?.userId, limit, skip, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun getLikedUsers(feedId: String, limit: Int, skip: Int) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.getLikedUsers(feedId, limit, skip, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun likeFeed(feedId: String) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.likeFeed(user?.userId, feedId, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun reportFeed(feedId: String, title: String, comment: String) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.reportFeed(user?.userId, feedId, title, comment, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun blockFeed(feedId: String, title: String, comment: String) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.blockFeed(user?.userId, feedId, title, comment, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

    fun sendComment(feedId: String, comment: String) = liveData {
        emit(State.loading(data = null))
        try {
            val token = getUserToken()
            val user = getUserData()
            if (token.isNotEmpty()) {
                emit(State.success(data = repository.sendComment(user?.userId, feedId, comment, token)))
            } else {
                emit(State.error(data = null, message = "User token missing"))
            }
        } catch (t: Throwable) {
            //An error was thrown
            t.printStackTrace()
            if (t is HttpException) {
                val errorMessage = getErrorMessageFromGenericResponse(t)
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

}