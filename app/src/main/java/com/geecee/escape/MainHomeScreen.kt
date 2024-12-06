package com.geecee.escape

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme
import com.geecee.escape.ui.views.FirstTime
import com.geecee.escape.ui.views.HomeScreenPageManager
import com.geecee.escape.ui.views.Settings
import com.geecee.escape.ui.views.Setup
import com.geecee.escape.utils.ChallengesManager
import com.geecee.escape.utils.FavoriteAppsManager
import com.geecee.escape.utils.HiddenAppsManager
import com.geecee.escape.utils.ScreenTimeManager
import com.geecee.escape.utils.getBooleanSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// A class to send data around the app more easily
data class MainAppModel(
    var context: Context,
    var packageManager: PackageManager,
    var favoriteAppsManager: FavoriteAppsManager,
    var hiddenAppsManager: HiddenAppsManager,
    var challengesManager: ChallengesManager,
    var isAppOpened: Boolean,
    var currentPackageName: String? = null,
    var coroutineScope: CoroutineScope
)

@Suppress("DEPRECATION")
class MainHomeScreen : ComponentActivity() {
    private lateinit var mainAppModel: MainAppModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureFullScreenMode()
        ScreenTimeManager.initialize(this)
        setContent { SetUpContent() }
    }

    override fun onResume() {
        super.onResume()

        try {

            // Update screen time on app when you come home
            if (mainAppModel.isAppOpened) {
                if (mainAppModel.currentPackageName != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        ScreenTimeManager.onAppClosed(mainAppModel.currentPackageName!!)

                        mainAppModel.currentPackageName = null
                    }
                }
                mainAppModel.isAppOpened = false
            }


        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
        }
    }

    // Makes it fullscreen
    private fun configureFullScreenMode() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    // Set content
    @Composable
    private fun SetUpContent() {
        EscapeTheme {
            InitializeMainAppModel()
            val startDestination = determineStartDestination()
            SetupNavHost(startDestination)
        }
    }

    // Sets properties to initialize MainAppModel
    @Composable
    private fun InitializeMainAppModel() {
        val context = LocalContext.current
        mainAppModel = MainAppModel(
            context = context,
            packageManager = packageManager,
            favoriteAppsManager = FavoriteAppsManager(context),
            hiddenAppsManager = HiddenAppsManager(context),
            challengesManager = ChallengesManager(context),
            false,
            coroutineScope = rememberCoroutineScope()
        )
    }

    // Finds which screen to start on
    private fun determineStartDestination(): String {
        return when {
            getBooleanSetting(mainAppModel.context,"hasDoneSetupPageOne", false) -> "setup"
            getBooleanSetting(mainAppModel.context,"FirstTime", true) -> "first_time"
            else -> "home"
        }
    }

    // Navigation host
    @Composable
    private fun SetupNavHost(startDestination: String) {
        val navController = rememberNavController()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, startDestination = startDestination) {
                composable("home",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    HomeScreenPageManager(mainAppModel) { navController.navigate("settings") }
                }
                composable("settings",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Settings(
                        mainAppModel,
                        { navController.popBackStack() },
                        this@MainHomeScreen,
                    )
                }
                composable("first_time",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    FirstTime { navController.navigate("setup") }
                }
                composable("setup",
                    enterTransition = { fadeIn(tween(900)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Setup(mainAppModel) { navController.navigate("home") }
                }
            }
        }
    }
}
