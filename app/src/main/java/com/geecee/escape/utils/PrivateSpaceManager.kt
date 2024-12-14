@file:Suppress("unused")

package com.geecee.escape.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat.getSystemService

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
fun checkPrivateSpaceAndUpdateVariable(context: Context, variable: MutableState<Boolean>) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            val isQuietModeEnabled = userManager.isQuietModeEnabled(userInfo)
            if (isQuietModeEnabled) {
                variable.value = false
            }
        }
    }

    variable.value = true
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
fun unlockPrivateSpaceAndUpdateVariable(context: Context, variable: MutableState<Boolean>) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            userManager.requestQuietModeEnabled(false, userInfo)
            variable.value = true
        }
    }
}