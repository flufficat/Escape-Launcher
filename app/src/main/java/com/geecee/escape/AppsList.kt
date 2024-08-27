package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    packageManager: PackageManager,
    context: Context,
    onCloseAppDrawer: () -> Unit,
    favoriteAppsManager: FavoriteAppsManager,
    hiddenAppsManager: HiddenAppsManager
) {
    val haptics = LocalHapticFeedback.current
    val installedApps = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.GET_ACTIVITIES
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentSelectedApp by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(favoriteAppsManager.isAppFavorite(currentPackageName)) }
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(0.dp, 50.dp, 0.dp, 0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = if (sharedPreferencesSettings.getString(
                    "AppsAlignment", "Center"
                ) == "Center"
            ) Alignment.CenterHorizontally else if (sharedPreferencesSettings.getString(
                    "AppsAlignment", "Center"
                ) == "Left"
            ) Alignment.Start else Alignment.End,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(30.dp, 0.dp, 30.dp, 140.dp)
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "All Apps", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))


            installedApps.sortedBy { it.loadLabel(packageManager).toString() }.forEach { appInfo ->
                if (!hiddenAppsManager.isAppHidden(appInfo.activityInfo.packageName) && appInfo.activityInfo.packageName != "com.geecee.escape") {
                    Text(
                        appInfo.loadLabel(packageManager).toString(),
                        modifier = Modifier
                            .padding(0.dp, 15.dp)
                            .combinedClickable(onClick = {
                                val launchIntent =
                                    packageManager.getLaunchIntentForPackage(appInfo.activityInfo.packageName)
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    val options = ActivityOptions.makeBasic()
                                    context.startActivity(launchIntent, options.toBundle())
                                }
                                onCloseAppDrawer()
                            }, onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showBottomSheet = true
                                currentSelectedApp = appInfo
                                    .loadLabel(packageManager)
                                    .toString()
                                currentPackageName = appInfo.activityInfo.packageName
                                isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 50.dp),
        horizontalArrangement = if (sharedPreferencesSettings.getString(
                "AppsAlignment", "Center"
            ) == "Center"
        ) Arrangement.Center else if (sharedPreferencesSettings.getString(
                "AppsAlignment", "Center"
            ) == "Left"
        ) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Button(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onCloseAppDrawer()
        }) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                "Open app drawer",
                tint = MaterialTheme.colorScheme.background
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; }, sheetState = sheetState) {
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
                        currentSelectedApp,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                    Text(
                        "Uninstall",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                // Uninstall logic here
                                val intent = Intent(
                                    Intent.ACTION_DELETE, Uri.parse("package:$currentPackageName")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Hide",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                hiddenAppsManager.addHiddenApp(currentPackageName)
                                showBottomSheet = false
                            }), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (isFavorite) "Remove from favourites" else "Add to favourites",
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (isFavorite) {
                                    favoriteAppsManager.removeFavoriteApp(currentPackageName)
                                } else {
                                    favoriteAppsManager.addFavoriteApp(currentPackageName)
                                }
                                // Update the state after the operation
                                isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Add Open Challenge",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "App Info",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:$currentPackageName")
                                    }
                                context.startActivity(intent)
                                showBottomSheet = false
                            }), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
