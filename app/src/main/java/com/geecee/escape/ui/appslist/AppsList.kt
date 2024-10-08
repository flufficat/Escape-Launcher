package com.geecee.escape

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.ui.theme.JostTypography
import java.util.Calendar

data class AppsListState(
    var hasSearchedOpenApp: Boolean = false,
    var currentPackageName: String = "",
    var currentSelectedApp: String = "",
    var isFavorite: Boolean = false,
    var isChallenge: Boolean = false,
    var autoOpen: Boolean = false
)

const val APPS_ALIGNMENT = "AppsAlignment"
const val SEARCH_AUTO_OPEN = "searchAutoOpen"

fun filterAndSortApps(
    apps: List<ResolveInfo>,
    searchText: String,
    packageManager: PackageManager
): List<ResolveInfo> {
    return apps.filter { appInfo ->
        val appName = appInfo.loadLabel(packageManager).toString()
        appName.contains(searchText, ignoreCase = true)
    }.sortedBy { it.loadLabel(packageManager).toString() }
}

fun getAlignmentFromPreferences(preferences: SharedPreferences): Alignment.Horizontal {
    return when (preferences.getString(APPS_ALIGNMENT, "Center")) {
        "Center" -> Alignment.CenterHorizontally
        "Left" -> Alignment.Start
        else -> Alignment.End
    }
}

fun shouldShowSearchBox(preferences: SharedPreferences): Boolean {
    return preferences.getString("showSearchBox", "False") == "True"
}

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    packageManager: PackageManager,
    context: Context,
    onCloseAppDrawer: () -> Unit,
    favoriteAppsManager: FavoriteAppsManager,
    hiddenAppsManager: HiddenAppsManager,
    challengesManager: ChallengesManager
) {
    val haptics = LocalHapticFeedback.current
    val installedApps by remember {
        mutableStateOf(
            packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.GET_ACTIVITIES
            ).toMutableList()
        )
    }
    val sheetState = rememberModalBottomSheetState()
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val state = remember { mutableStateOf(AppsListState()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var searchBoxText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    if (sharedPreferencesSettings.getString(SEARCH_AUTO_OPEN, "False") == "True") {
        state.value.autoOpen = true
    }

    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = endTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startTime = calendar.timeInMillis + 1000
    usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startTime, endTime
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(0.dp, 50.dp, 0.dp, 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize() // Ensure the list takes up all available space
                    .weight(1f) // Weight allows LazyColumn to scroll freely within the available space
                    .padding(30.dp, 0.dp, 30.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = getAlignmentFromPreferences(sharedPreferencesSettings),

                ) {
                // Title
                item {
                    Spacer(modifier = Modifier.height(140.dp))
                    Text(
                        text = stringResource(id = R.string.all_apps),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Search bar
                if (shouldShowSearchBox(sharedPreferencesSettings) &&
                    !state.value.hasSearchedOpenApp
                ) {
                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                        AnimatedPillSearchBar({ searchText ->
                            searchBoxText = searchText

                            if (state.value.autoOpen) {
                                if (filterAndSortApps(
                                        installedApps,
                                        searchText,
                                        packageManager
                                    ).size == 1
                                ) { // If there is exactly one app matching the search
                                    state.value.hasSearchedOpenApp = true
                                    val appInfo = filterAndSortApps(
                                        installedApps,
                                        searchText,
                                        packageManager
                                    ).first()
                                    state.value.currentPackageName =
                                        appInfo.activityInfo.packageName

                                    onCloseAppDrawer()
                                    val launchIntent =
                                        packageManager.getLaunchIntentForPackage(
                                            state.value.currentPackageName
                                        )
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        val options = ActivityOptions.makeBasic()
                                        context.startActivity(
                                            launchIntent,
                                            options.toBundle()
                                        )
                                    }
                                    state.value.hasSearchedOpenApp = false

                                }
                            }
                        },
                            { searchText ->
                                if (filterAndSortApps(
                                        installedApps,
                                        searchText,
                                        packageManager
                                    ).isNotEmpty()
                                ) {
                                    val firstAppInfo = filterAndSortApps(
                                        installedApps,
                                        searchText,
                                        packageManager
                                    ).first()
                                    val packageName = firstAppInfo.activityInfo.packageName
                                    state.value.currentPackageName = packageName
                                    if (challengesManager.doesAppHaveChallenge(state.value.currentPackageName)) {
                                        showDialog = true
                                    } else {
                                        AppUtils.openApp(
                                            packageManager,
                                            context,
                                            state.value.currentPackageName,
                                            challengesManager,
                                            false,
                                            null
                                        )
                                        onCloseAppDrawer()
                                    }
                                }
                            })
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }

                // App List
                items(installedApps.filter { appInfo ->
                    val appName = appInfo.loadLabel(packageManager).toString()
                    appName.contains(searchBoxText, ignoreCase = true)
                }.sortedBy { it.loadLabel(packageManager).toString() }) { appInfo ->
                    if (!hiddenAppsManager.isAppHidden(appInfo.activityInfo.packageName) &&
                        appInfo.activityInfo.packageName != "com.geecee.escape"
                    ) {


                        Text(
                            appInfo.loadLabel(packageManager)
                                .toString() + " " + AppUtils.getScreenTimeForPackage(
                                context,
                                appInfo.activityInfo.packageName
                            ),
                            modifier = Modifier
                                .padding(vertical = 15.dp)
                                .combinedClickable(
                                    onClick = {
                                        val packageName = appInfo.activityInfo.packageName
                                        state.value.currentPackageName = packageName
                                        if (challengesManager.doesAppHaveChallenge(packageName)) {
                                            showDialog = true
                                        } else {
                                            val launchIntent =
                                                packageManager.getLaunchIntentForPackage(packageName)
                                            if (launchIntent != null) {
                                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                context.startActivity(launchIntent)
                                            }
                                            onCloseAppDrawer()
                                        }
                                    },
                                    onLongClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showBottomSheet = true
                                        state.value.currentSelectedApp =
                                            appInfo
                                                .loadLabel(packageManager)
                                                .toString()
                                        state.value.currentPackageName =
                                            appInfo.activityInfo.packageName
                                        state.value.isFavorite =
                                            favoriteAppsManager.isAppFavorite(state.value.currentPackageName)
                                        state.value.isChallenge =
                                            challengesManager.doesAppHaveChallenge(state.value.currentPackageName)


                                    }
                                ),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(140.dp)) } //Spacer for bottom
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false; },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                    Row {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "App Options",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(45.dp)
                                .padding(0.dp, 0.dp, 10.dp, 0.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            state.value.currentSelectedApp,
                            Modifier,
                            MaterialTheme.colorScheme.primary,
                            fontSize = 32.sp,
                            style = JostTypography.titleMedium
                        )
                    }
                    HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                    Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                        Text(
                            stringResource(id = R.string.uninstall),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    // Uninstall logic here
                                    val intent = Intent(
                                        Intent.ACTION_DELETE,
                                        Uri.parse("package:$state.value.currentPackageName")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stringResource(id = R.string.hide),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    hiddenAppsManager.addHiddenApp(state.value.currentPackageName)
                                    showBottomSheet = false
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (state.value.isFavorite) stringResource(id = R.string.rem_from_fav) else stringResource(
                                id = R.string.add_to_fav
                            ),
                            modifier = Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    if (state.value.isFavorite) {
                                        favoriteAppsManager.removeFavoriteApp(state.value.currentPackageName)
                                    } else {
                                        favoriteAppsManager.addFavoriteApp(state.value.currentPackageName)
                                    }
                                    // Update the state after the operation
                                    state.value.isFavorite =
                                        favoriteAppsManager.isAppFavorite(state.value.currentPackageName)
                                }),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!state.value.isChallenge) {
                            Text(
                                stringResource(id = R.string.add_open_challenge),
                                Modifier
                                    .padding(0.dp, 10.dp)
                                    .combinedClickable(onClick = {
                                        challengesManager.addChallengeApp(state.value.currentPackageName)
                                        showBottomSheet = false
                                    }),
                                MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            stringResource(id = R.string.app_info),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.parse("package:$state.value.currentPackageName")
                                        }
                                    context.startActivity(intent)
                                    showBottomSheet = false
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }


        // Floating Close Button
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onCloseAppDrawer()
            },
            modifier = Modifier
                .align(
                    if (sharedPreferencesSettings.getString(
                            "AppsAlignment", "Center"
                        ) == "Center"
                    ) Alignment.BottomCenter else if (sharedPreferencesSettings.getString(
                            "AppsAlignment", "Center"
                        ) == "Left"
                    ) Alignment.BottomStart else Alignment.BottomEnd
                )
                .padding(30.dp)
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Close app drawer",
                tint = MaterialTheme.colorScheme.background
            )
        }


    }

    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge({
            val launchIntent =
                packageManager.getLaunchIntentForPackage(state.value.currentPackageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptions.makeBasic()
                context.startActivity(launchIntent, options.toBundle())
            }
            onCloseAppDrawer()
            state.value.hasSearchedOpenApp = false
        }, {
            state.value.hasSearchedOpenApp = false
            onCloseAppDrawer()
        })
    }
}
