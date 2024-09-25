package com.geecee.escape

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.JostTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Setup(
    packageManager: PackageManager,
    context: Context,
    favoriteAppsManager: FavoriteAppsManager,
    goHome: () -> Unit
) {
    val navController = rememberNavController()
    fun getInstalledApps(): List<android.content.pm.ResolveInfo> {
        return packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.GET_ACTIVITIES
        )
    }

    val installedApps = remember { mutableStateOf(getInstalledApps()) }
    fun updateFavoriteStatus(packageName: String, isFavorite: Boolean) {
        installedApps.value = getInstalledApps() // Refresh the list
        if (isFavorite) {
            favoriteAppsManager.addFavoriteApp(packageName)
        } else {
            favoriteAppsManager.removeFavoriteApp(packageName)
        }
    }

    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val favoritedApps = installedApps.value.filter { appInfo ->
        favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName)
    }.sortedBy { it.loadLabel(packageManager).toString() }
    val nonFavoritedApps = installedApps.value.filter { appInfo ->
        !favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName) && appInfo.activityInfo.packageName != "com.geecee.escape"
    }.sortedBy { it.loadLabel(packageManager).toString() }

    val startDestination: String =
        if (sharedPreferencesSettings.getString("hasDoneSetupPageOne", "False") == "True") {
            "launcher"
        } else {
            "choose_apps"
        }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("choose_apps", enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, tween(300)
            )
        }, exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, tween(300)
            )
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(200.dp))

                    Text(
                        text = stringResource(id = R.string.hi),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(id = R.string.choose_apps),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            navController.navigate("launcher")
                            val editor = sharedPreferencesSettings.edit()
                            editor.putString("hasDoneSetupPageOne", "True")
                            editor.apply()
                        },
                        modifier = Modifier.padding(0.dp, 16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            "Continue Home",
                            tint = MaterialTheme.colorScheme.background
                        )
                    }

                    Column {
                        favoritedApps.forEach { appInfo ->
                            Text(
                                appInfo.loadLabel(packageManager).toString(),
                                modifier = Modifier
                                    .padding(0.dp, 15.dp)
                                    .combinedClickable(onClick = {
                                        favoriteAppsManager.removeFavoriteApp(appInfo.activityInfo.packageName)
                                        updateFavoriteStatus(
                                            appInfo.activityInfo.packageName, false
                                        )
                                    }),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 15.dp))

                    Column {
                        nonFavoritedApps.forEach { appInfo ->
                            Text(
                                appInfo.loadLabel(packageManager).toString(),
                                modifier = Modifier
                                    .padding(0.dp, 15.dp)
                                    .combinedClickable(onClick = {
                                        favoriteAppsManager.addFavoriteApp(appInfo.activityInfo.packageName)
                                        updateFavoriteStatus(appInfo.activityInfo.packageName, true)
                                    }),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        composable("launcher", enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, tween(300)
            )
        }, exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, tween(300)
            )
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                ) {

                    Text(
                        text = stringResource(id = R.string.set_launcher),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(id = R.string.you_need_to_set_escape),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(0.dp, 16.dp)
                    ) {
                        Text(
                            stringResource(id = R.string.set_launcher_button),
                            Modifier,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(0.dp, 16.dp)
                    ) {
                        Text(
                            stringResource(id = R.string.set_launcher_button),
                            Modifier,
                            color = MaterialTheme.colorScheme.background
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.set_wallpaper),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            goHome()

                            val editor = sharedPreferencesSettings.edit()
                            editor.putString("FirstTime", "False")
                            editor.putString("hasDoneSetupPageOne", "False")
                            editor.apply()
                        },
                        modifier = Modifier.padding(0.dp, 16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            "Continue",
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SetupPrev() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Hi.",
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleMedium
            )
            Text(
                text = stringResource(id = R.string.choose_apps),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium
            )

            Button(
                onClick = {

                },
                modifier = Modifier.padding(0.dp, 16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    "Continue Home",
                    tint = MaterialTheme.colorScheme.background
                )
            }

            Column {
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
            }

            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 15.dp))

            Column {
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
                Text(
                    "App",
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
                )
            }
        }
    }
}


