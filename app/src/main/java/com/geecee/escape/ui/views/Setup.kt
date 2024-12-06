package com.geecee.escape.ui.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.JostTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Setup(
    mainAppModel: MainAppModel,
    goHome: () -> Unit
) {
    val navController = rememberNavController()
    fun getInstalledApps(): List<android.content.pm.ResolveInfo> {
        return mainAppModel.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.GET_ACTIVITIES
        )
    }

    val installedApps = remember { mutableStateOf(getInstalledApps()) }
    fun updateFavoriteStatus(packageName: String, isFavorite: Boolean) {
        installedApps.value = getInstalledApps() // Refresh the list
        if (isFavorite) {
            mainAppModel.favoriteAppsManager.addFavoriteApp(packageName)
        } else {
            mainAppModel.favoriteAppsManager.removeFavoriteApp(packageName)
        }
    }

    val sharedPreferencesSettings: SharedPreferences = mainAppModel.context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val favoritedApps = installedApps.value.filter { appInfo ->
        mainAppModel.favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName)
    }.sortedBy { it.loadLabel(mainAppModel.packageManager).toString() }
    val nonFavoritedApps = installedApps.value.filter { appInfo ->
        !mainAppModel.favoriteAppsManager.isAppFavorite(appInfo.activityInfo.packageName) && appInfo.activityInfo.packageName != "com.geecee.escape"
    }.sortedBy { it.loadLabel(mainAppModel.packageManager).toString() }

    val startDestination: String =
        if (sharedPreferencesSettings.getBoolean("hasDoneSetupPageOne", false)) {
            "launcher"
        } else {
            "choose_apps"
        }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("choose_apps",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
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
                        color = MaterialTheme.colorScheme.onBackground,
                        style = JostTypography.titleMedium
                    )
                    Text(
                        text = stringResource(id = R.string.choose_apps),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = JostTypography.bodyMedium
                    )

                    Button(
                        onClick = {
                            navController.navigate("launcher")
                            val editor = sharedPreferencesSettings.edit()
                            editor.putBoolean("hasDoneSetupPageOne", true)
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
                                appInfo.loadLabel(mainAppModel.packageManager).toString(),
                                modifier = Modifier
                                    .padding(0.dp, 15.dp)
                                    .combinedClickable(onClick = {
                                        mainAppModel.favoriteAppsManager.removeFavoriteApp(appInfo.activityInfo.packageName)
                                        updateFavoriteStatus(
                                            appInfo.activityInfo.packageName, false
                                        )
                                    }),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = JostTypography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 15.dp)
                    )

                    Column {
                        nonFavoritedApps.forEach { appInfo ->
                            Text(
                                appInfo.loadLabel(mainAppModel.packageManager).toString(),
                                modifier = Modifier
                                    .padding(0.dp, 15.dp)
                                    .combinedClickable(onClick = {
                                        mainAppModel.favoriteAppsManager.addFavoriteApp(appInfo.activityInfo.packageName)
                                        updateFavoriteStatus(appInfo.activityInfo.packageName, true)
                                    }),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = JostTypography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        composable("launcher",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }) {
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
                        color = MaterialTheme.colorScheme.onBackground,
                        style = JostTypography.titleMedium
                    )
                    Text(
                        text = stringResource(id = R.string.you_need_to_set_escape),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = JostTypography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                            mainAppModel.context.startActivity(intent)
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
                            val intent =
                                Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            mainAppModel.context.startActivity(intent)
                        },
                        modifier = Modifier.padding(0.dp, 16.dp)
                    ) {
                        Text(
                            stringResource(id = R.string.allow_screen_time),
                            Modifier,
                            color = MaterialTheme.colorScheme.background
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.set_wallpaper),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = JostTypography.bodyMedium
                    )

                    Button(
                        onClick = {
                            goHome()

                            val editor = sharedPreferencesSettings.edit()
                            editor.putBoolean("FirstTime", false)
                            editor.putBoolean("hasDoneSetupPageOne", false)
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