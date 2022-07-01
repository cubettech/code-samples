package com.hrmfitclub.android.injection.module

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.hrmfitclub.android.data.ApiRepository
import com.hrmfitclub.android.view.ViewConstants
import org.koin.dsl.module
import java.util.*

val repositoryModule = module {
    factory { ApiRepository(get()) }

    single { provideSharedPreferences(get()) }
}

fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
}