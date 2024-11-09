package com.geecee.escape.utils

import android.content.Context
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService

fun isPrivateSpace(context: Context): Boolean {
    val userManager = getSystemService(context, UserManager::class.java) as UserManager

    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        val isQuietModeEnabled = userManager.isQuietModeEnabled(userInfo)
        if (isQuietModeEnabled) {
            return true
        }
    }
    return false
}

@RequiresApi(Build.VERSION_CODES.P)
fun lockPrivateSpace(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles
    for (userInfo in profiles) {
            userManager.requestQuietModeEnabled(true, userInfo)

    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun unlockPrivateSpace(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles
    for (userInfo in profiles) {
            userManager.requestQuietModeEnabled(false, userInfo)

    }
}