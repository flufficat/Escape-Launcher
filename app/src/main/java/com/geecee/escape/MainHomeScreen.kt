package com.geecee.escape

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme
import com.geecee.escape.ui.views.FirstTime
import com.geecee.escape.ui.views.Settings
import com.geecee.escape.ui.views.Setup
import com.geecee.escape.ui.views.SwipeableHome
import com.geecee.escape.utils.ChallengesManager
import com.geecee.escape.utils.FavoriteAppsManager
import com.geecee.escape.utils.HiddenAppsManager

data class MainAppModel(
    var context: Context,
    var packageManager: PackageManager,
    var favoriteAppsManager: FavoriteAppsManager,
    var hiddenAppsManager: HiddenAppsManager,
    var challengesManager: ChallengesManager
)

@Suppress("DEPRECATION")
class MainHomeScreen : ComponentActivity() {
    private lateinit var mainAppModel: MainAppModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureFullScreenMode()
        setContent { SetUpContent() }
    }

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

    @Composable
    private fun SetUpContent() {
        EscapeTheme {
            InitializeMainAppModel()
            val startDestination = determineStartDestination()
            SetupNavHost(startDestination)
        }
    }

    @Composable
    private fun InitializeMainAppModel() {
        val context = LocalContext.current
        mainAppModel = MainAppModel(
            context = context,
            packageManager = packageManager,
            favoriteAppsManager = FavoriteAppsManager(context),
            hiddenAppsManager = HiddenAppsManager(context),
            challengesManager = ChallengesManager(context)
        )
    }

    private fun determineStartDestination(): String {
        val sharedPreferencesSettings: SharedPreferences =
            mainAppModel.context.getSharedPreferences(
                R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
            )
        return when {
            sharedPreferencesSettings.getString("hasDoneSetupPageOne", "False") == "True" -> "setup"
            sharedPreferencesSettings.getString("FirstTime", "True") == "True" -> "first_time"
            else -> "home"
        }
    }

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
                    SwipeableHome(mainAppModel) { navController.navigate("settings") }
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
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Setup(
                        packageManager,
                        mainAppModel.context,
                        mainAppModel.favoriteAppsManager
                    ) { navController.navigate("home") }
                }
            }
        }
    }
}
