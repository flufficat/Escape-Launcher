@file:Suppress("unused")

package com.geecee.escape.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService

data class PrivateSpaceApp(
    var displayName: String,
    var packageName: String
)

class PrivateSpaceStateReceiver(private val onStateChange: (Boolean) -> Unit) : BroadcastReceiver() {
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
                privateSpaceApps.add(PrivateSpaceApp(displayName = appName, packageName = packageName))
            }
        }
    }

    return privateSpaceApps
}