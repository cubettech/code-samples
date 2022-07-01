package io.odinmanufacturing.applications

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.HiltAndroidApp
import io.odinmanufacturing.utils.OMPreferenceManager
import javax.inject.Inject

/**
 * Application class *
 * @property oPreferenceManager OMPreferenceManager
 */
@HiltAndroidApp
class OMApplication : Application() {
    companion object {
        lateinit var oMPreferenceManager: OMPreferenceManager
    }
    @Inject
    lateinit var oPreferenceManager: OMPreferenceManager
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        oMPreferenceManager = oPreferenceManager
    }
}
