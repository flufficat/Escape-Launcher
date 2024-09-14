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
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme

@Suppress("DEPRECATION")
class MainHomeScreen : ComponentActivity() {
    private lateinit var packageManager: PackageManager
    private lateinit var context: Context
    private lateinit var favoriteAppsManager: FavoriteAppsManager
    private lateinit var hiddenAppsManager: HiddenAppsManager
    private lateinit var challengesManager: ChallengesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setContent {
            EscapeTheme {
                context = LocalContext.current
                packageManager = context.packageManager
                favoriteAppsManager = FavoriteAppsManager(context)
                hiddenAppsManager = HiddenAppsManager(context)
                challengesManager = ChallengesManager(context)


                val startDestination: String
                val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
                    R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
                )
                startDestination = if (sharedPreferencesSettings.getString(
                        "hasDoneSetupPageOne",
                        "False"
                    ) == "True"
                ) {
                    "setup"
                } else if (sharedPreferencesSettings.getString("FirstTime", "True") == "True") {
                    "first_time"
                } else {
                    "home"
                }

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


                            HomeScreen(
                                onOpenAppDrawer = { navController.navigate("app_drawer") },
                                onOpenSettings = { navController.navigate("settings") },
                                packageManager = packageManager,
                                context = context,
                                favoriteAppsManager = favoriteAppsManager
                            )


                        }
                        composable(
                            "app_drawer",
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Up, tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down, tween(300)
                                )
                            },
                        ) {
                            AppDrawer(
                                packageManager,
                                context,
                                onCloseAppDrawer = {
                                    navController.popBackStack()
                                },
                                favoriteAppsManager = favoriteAppsManager,
                                hiddenAppsManager,
                                challengesManager
                            )
                        }
                        composable("settings",
                            enterTransition = { fadeIn(tween(300)) },
                            exitTransition = { fadeOut(tween(300)) }) {
                            Settings(
                                context,
                                { navController.popBackStack() },
                                this@MainHomeScreen,
                                packageManager = packageManager,
                                hiddenAppsManager = hiddenAppsManager,
                                challengesManager = challengesManager
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
                                packageManager, context, favoriteAppsManager
                            ) { navController.navigate("home") }
                        }
                    }
                }
            }
        }
    }
}

