package com.geecee.escape

import android.app.ActivityOptions
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.runtime.MutableState
import java.util.concurrent.TimeUnit

object AppUtils {
    fun openApp(packageManager: PackageManager, context: Context, packageName: String,challengesManager: ChallengesManager, overrideOpenChallenge: Boolean, openChallengeShow: MutableState<Boolean>?){
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            if(!challengesManager.doesAppHaveChallenge(packageName) || overrideOpenChallenge){
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptions.makeBasic()
                context.startActivity(launchIntent, options.toBundle())
            }
            else{
                if(openChallengeShow != null){
                    openChallengeShow.value = true;
                }
            }
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

    fun getAllInstalledApps(packageManager: PackageManager): MutableList<ResolveInfo>{
        return packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.GET_ACTIVITIES
        ).toMutableList()
    }

    fun getAppNameFromPackageName(context: Context, packageName: String): String {
        return try {
            val packageManager: PackageManager = context.packageManager

            val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)

            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "null"
        }
    }
}