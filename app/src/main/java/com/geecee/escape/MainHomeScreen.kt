package com.geecee.escape

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme

fun getAppNameFromPackageName(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown App"
    }
}

class MainHomeScreen : ComponentActivity() {
    private lateinit var packageManager: PackageManager
    private lateinit var context: Context
    private lateinit var favoriteAppsManager: FavoriteAppsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            EscapeTheme {
                context = LocalContext.current
                packageManager = context.packageManager
                favoriteAppsManager = FavoriteAppsManager(context)

                val navController = rememberNavController()

                Box(
                    modifier = Modifier.fillMaxSize()
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
                        composable("app_drawer") {
                            AppDrawer(
                                packageManager,
                                context,
                                onCloseAppDrawer = {
                                    navController.popBackStack()
                                },
                                favoriteAppsManager = favoriteAppsManager
                            )
                        }
                        composable("settings") {
                            SettingsScreen(context) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }
}

