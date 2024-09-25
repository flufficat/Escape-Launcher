package com.geecee.escape

import android.app.ActivityOptions
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.concurrent.TimeUnit

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

    fun getScreenTimeForPackage(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Last 24 hours

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        var totalUsageTime: Long = 0

        usageStatsList?.forEach { usageStats ->
            if (usageStats.packageName == packageName) {
                totalUsageTime += usageStats.totalTimeInForeground
            }
        }

        return totalUsageTime // Return total usage time in milliseconds
    }
}