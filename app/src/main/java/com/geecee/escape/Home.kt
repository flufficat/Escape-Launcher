package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    packageManager: PackageManager,
    context: Context,
    favoriteAppsManager: FavoriteAppsManager
) {
    val haptics = LocalHapticFeedback.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentSelectedApp by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(favoriteAppsManager.isAppFavorite(currentPackageName)) }
    val favoriteApps = remember { mutableStateOf(favoriteAppsManager.getFavoriteApps()) }
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = {}, onLongClickLabel = {}.toString(), onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenSettings()
            })
    ) {
        Column(
            verticalArrangement = if (sharedPreferencesSettings.getString(
                    "HomeVAlignment", "Center"
                ) == "Center"
            ) Arrangement.Center else if (sharedPreferencesSettings.getString(
                    "HomeVAlignment", "Center"
                ) == "Top"
            ) Arrangement.Top else Arrangement.Bottom,
            horizontalAlignment = if (sharedPreferencesSettings.getString(
                    "HomeAlignment", "Center"
                ) == "Center"
            ) Alignment.CenterHorizontally else if (sharedPreferencesSettings.getString(
                    "HomeAlignment", "Center"
                ) == "Left"
            ) Alignment.Start else Alignment.End,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(30.dp, 90.dp, 30.dp, 30.dp)
        ) {

            Clock(sharedPreferencesSettings)

            var widgetOffset by remember { mutableIntStateOf(0) }
            widgetOffset = if (sharedPreferencesSettings.getString("HomeAlignment", "Center") == "Left") {
                -10
            } else if (sharedPreferencesSettings.getString("HomeAlignment", "Center") == "Right") {
                8
            } else {
                0
            }

            if (sharedPreferencesSettings.getString("WidgetsToggle", "False") == "True") {
                WidgetsScreen(
                    context = context,
                    modifier = Modifier
                        .offset((widgetOffset).dp, 0.dp)
                        .size(150.dp, 100.dp)
                )
            }

            for (app in favoriteApps.value) {
                Text(
                    getAppNameFromPackageName(context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent = packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showBottomSheet = true
                            currentSelectedApp = getAppNameFromPackageName(context, app)
                            currentPackageName = app
                            isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenAppDrawer()
            }) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    "Open app drawer",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; }, sheetState = sheetState) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = Color.White,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        currentSelectedApp,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp
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
                                Log.d("PACKAGE NAME", "package:$currentPackageName")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }), MaterialTheme.colorScheme.primary, fontSize = 25.sp
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
                                favoriteApps.value = favoriteAppsManager.getFavoriteApps()
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
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
                            }), MaterialTheme.colorScheme.primary, fontSize = 25.sp
                    )
                }
            }
        }
    }
}

fun getCurrentTime(): String {
    val now = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm") // Format as hours:minutes:seconds
    return now.format(formatter)
}

@Composable
fun Clock(sharedPreferencesSettings: SharedPreferences) {
    var time by remember { mutableStateOf(getCurrentTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime()
            delay(1000) // Update every second
        }
    }

    Text(
        text = time,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 48.sp,
        modifier = if (sharedPreferencesSettings.getString(
                "HomeAlignment",
                "Center"
            ) == "Left"
        ) Modifier.offset((-4).dp) else if (sharedPreferencesSettings.getString(
                "HomeAlignment",
                "Center"
            ) == "Right"
        ) Modifier.offset(0.dp) else Modifier.offset(0.dp)
    )
}

fun getAppNameFromPackageName(context: Context, packageName: String): String {
    return try {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown App"
    }
}