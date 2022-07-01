package io.odinmanufacturing

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.odinmanufacturing.screens.route.ScreenRoute
import io.odinmanufacturing.screens.splash.ScreenSplash

/**
 * This class is used for displaying the splash animation and registering basic controls.
 * @property navController NavHostController
 */
class OMSplashActivity : ComponentActivity() {
    lateinit var navController: NavHostController

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val closeDialog = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(closeDialog)
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        setContent {
            ScreenNav()
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun ScreenNav() {
        navController = rememberNavController()
        NavHost(navController, startDestination = getStartDestination()) {
            composable(route = ScreenRoute.SplashScreen.route) {
                ScreenSplash {
                    navigateToMainScreen()
                }
            }
        }
    }

    /**
     * Navigate to OMMainActivity screen
     */
    private fun navigateToMainScreen() {
        val intent = Intent(applicationContext, OMMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Returns current screen route
     * @return String
     */
    private fun getStartDestination(): String {
        return ScreenRoute.SplashScreen.route
    }
}
