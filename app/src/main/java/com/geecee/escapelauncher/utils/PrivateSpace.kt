package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.transparentHalf
import com.geecee.escapelauncher.ui.views.SettingsSwitch

data class PrivateSpaceApp(
    var displayName: String,
    var packageName: String,
    var componentName: ComponentName
)

class PrivateSpaceStateReceiver(private val onStateChange: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PROFILE_AVAILABLE -> {
                onStateChange(true) // Private space is unlocked
            }

            Intent.ACTION_PROFILE_UNAVAILABLE -> {
                onStateChange(false) // Private space is locked
            }
        }
    }
}

/**
 * Find out if private space is unlocked
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isPrivateSpace(context: Context): Boolean {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            val isQuietModeEnabled = userManager.isQuietModeEnabled(userInfo)
            if (isQuietModeEnabled) {
                return false
            }
        }
    }

    return true
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun lockPrivateSpace(context: Context) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            userManager.requestQuietModeEnabled(true, userInfo)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun unlockPrivateSpace(context: Context) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            userManager.requestQuietModeEnabled(false, userInfo)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun getPrivateSpaceApps(context: Context): List<PrivateSpaceApp> {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles
    val privateSpaceApps = mutableListOf<PrivateSpaceApp>()

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            val activityList = launcherApps.getActivityList(null, userInfo)
            for (activity in activityList) {
                val appName = activity.label?.toString() ?: "Unknown App"
                val packageName = activity.applicationInfo.packageName
                privateSpaceApps.add(
                    PrivateSpaceApp(
                        displayName = appName,
                        packageName = packageName,
                        componentName = activity.componentName
                    )
                )
            }
        }
    }

    return privateSpaceApps
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivateAppItem(
    appName: String,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Text(
        appName,
        modifier = Modifier
            .padding(vertical = 15.dp)
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = {
                    onLongClick()
                }
            ),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun PrivateSpaceSettings(context: Context, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .clickable {
                onDismiss()
            }
            .background(transparentHalf))
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(20.dp, 0.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .shadow(
                    5.dp,
                    MaterialTheme.shapes.extraLarge,
                    ambientColor = MaterialTheme.colorScheme.scrim
                )
                .clickable {},
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.private_space_settings),
                    style = MaterialTheme.typography.bodyLarge
                )

                SettingsSwitch(
                    stringResource(R.string.hide_private_space_in_search),
                    getBooleanSetting(context, stringResource(R.string.SearchHiddenPrivateSpace), false)
                ) { value ->
                    setBooleanSetting(context, context.resources.getString(R.string.SearchHiddenPrivateSpace)  , value)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun openPrivateSpaceApp(privateSpaceApp: PrivateSpaceApp, context: Context, sourceBounds: Rect) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            println("Context Type: ${context.javaClass.name}")
            val options = ActivityOptions.makeBasic()
            launcherApps.startMainActivity(
                privateSpaceApp.componentName,
                userInfo,
                sourceBounds,
                options.toBundle()
            )
        }
    }
}