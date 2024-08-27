package com.geecee.escape

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
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

                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
                ) {
                    NavHost(navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onOpenAppDrawer = { navController.navigate("app_drawer") },
                                onOpenSettings = { navController.navigate("settings") },
                                context = context,
                                packageManager = packageManager,
                                favoriteAppsManager = favoriteAppsManager
                            )
                        }
                        composable(
                            "app_drawer",
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Up,
                                    tween(300)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down,
                                    tween(300)
                                )
                            },
                        ) {
                            AppDrawer(
                                packageManager, context, onCloseAppDrawer = {
                                    navController.popBackStack()
                                }, favoriteAppsManager = favoriteAppsManager, hiddenAppsManager
                            )
                        }
                        composable("settings",
                            enterTransition = { fadeIn(tween(300)) },
                            exitTransition = { fadeOut(tween(300)) }) {
                            SettingsScreen(
                                context,
                                { navController.popBackStack() },
                                { navController.navigate("hidden_apps") })
                        }
                        composable("hidden_apps",
                            enterTransition = { fadeIn(tween(300)) },
                            exitTransition = { fadeOut(tween(300)) }
                        ) {
                            HiddenAppsScreen(
                                context,
                                hiddenAppsManager = hiddenAppsManager,
                                packageManager
                            ) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}

