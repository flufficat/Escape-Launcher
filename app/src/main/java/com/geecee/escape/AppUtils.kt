package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppUtils {
    fun OpenApp(packageManager: PackageManager,context: Context, packageName: String){
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptions.makeBasic()
            context.startActivity(launchIntent, options.toBundle())
        }
    }
}