package com.hrmfitclub.android

import android.app.Application
import com.hrmfitclub.android.injection.module.remoteDatasourceModule
import com.hrmfitclub.android.injection.module.repositoryModule
import com.hrmfitclub.android.injection.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class HRMApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@HRMApplication)
            modules(listOf(remoteDatasourceModule,
                    repositoryModule,
                    viewModelModule))
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}