package com.hrmfitclub.android.injection.module

import com.google.gson.*
import com.hrmfitclub.android.BuildConfig
import com.hrmfitclub.android.data.remote.endpoint.HRMWebService
import com.hrmfitclub.android.misc.RequestInterceptor
import com.hrmfitclub.android.misc.UnsafeOkHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

val remoteDatasourceModule = module {

    //RequestInterceptor
    single { provideRequestInterceptor() }

    //LoggingInterceptor
    single { provideLoggingInterceptor() }

    // provided web components
    single { provideOkHttpClient(get(), get()) }

    single { provideGson() }

    single { provideRemoteDataSource(get(), get()) }
}

fun provideGson(): Gson {
    val builder = GsonBuilder()

    builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
        json?.asJsonPrimitive?.asLong?.let {
            return@JsonDeserializer Date(it)
        }
    })

    builder.registerTypeAdapter(Date::class.java, JsonSerializer<Date> { date, _, _ ->
        JsonPrimitive(date.time)
    })

    return builder.create()
}

fun provideRequestInterceptor(): RequestInterceptor {
    return RequestInterceptor()
}

fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    val logInterceptor = HttpLoggingInterceptor()
    logInterceptor.level = HttpLoggingInterceptor.Level.BODY

    return logInterceptor
}

fun provideOkHttpClient(requestInterceptor: RequestInterceptor,
                        logInterceptor: HttpLoggingInterceptor): OkHttpClient {

    val builder = UnsafeOkHttpClient.getUnsafeOkHttpClient()

    //add interceptors
    builder.addInterceptor(logInterceptor)
    builder.addInterceptor(requestInterceptor)

    builder.connectTimeout(2, TimeUnit.MINUTES)
    builder.readTimeout(1, TimeUnit.MINUTES)
    builder.readTimeout(1, TimeUnit.MINUTES)

    return builder.build()
}

fun provideRemoteDataSource(okHttpClient: OkHttpClient, gson: Gson): HRMWebService {
    return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.URL_API)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(HRMWebService::class.java)
}
