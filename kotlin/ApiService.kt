package com.android.pause.data.remote

import com.android.pause.BuildConfig
import com.android.pause.data.model.UserResponse
import com.android.pause.data.model.itineraries.ItineraryResponse
import com.android.pause.data.model.normalResponse.NormalResponse
import com.android.pause.util.MyGsonTypeAdapterFactory
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("username") email: String, @Field("password") password: String): Observable<UserResponse>

    @FormUrlEncoded
    @POST("itinerary")
    fun fetchItineraries(@Header("Authorization") token: String, @Field("user_id") userId: String): Observable<ItineraryResponse>

    @GET("logout")
    fun logout(@Header("Authorization") token: String, @Field("user_id") userId: String): Observable<NormalResponse>

    @FormUrlEncoded
    @POST("user_reset_password")
    fun forgotPassword(@Field("user_email") email: String): Observable<NormalResponse>

    object Creator {

        fun newRibotsService(): ApiService {
            val gson = GsonBuilder()
                    .registerTypeAdapterFactory(MyGsonTypeAdapterFactory.create())
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create()
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val builder = OkHttpClient().newBuilder()
            builder.connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
            val okHttpClient = builder.addInterceptor(logInterceptor)
                    .build()
            val retrofit = Retrofit.Builder()
                    .baseUrl(ApiService.ENDPOINT)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return retrofit.create(ApiService::class.java)
        }

    }

    companion object {

        /* Original API and related calls are removed here
    *
    * */
        val ENDPOINT = BuildConfig.BASE_URL_CONSTANT
    }
}
