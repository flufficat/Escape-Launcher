@file:Suppress("unused")

package com.geecee.escape.utils

import android.app.ActivityOptions
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import com.geecee.escape.MainAppModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit


object AppUtils {
    fun openApp(
        packageName: String,
        overrideOpenChallenge: Boolean,
        openChallengeShow: MutableState<Boolean>?,
        mainAppModel: MainAppModel
    ) {
        val launchIntent =
            mainAppModel.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            if (!mainAppModel.challengesManager.doesAppHaveChallenge(packageName) || overrideOpenChallenge) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptions.makeBasic()
                mainAppModel.context.startActivity(launchIntent, options.toBundle())

                ScreenTimeManager.onAppOpened(packageName)

                mainAppModel.isAppOpened = true
                mainAppModel.currentPackageName = packageName
            } else {
                if (openChallengeShow != null) {
                    openChallengeShow.value = true
                }
            }
        }
    }

    fun getScreenTimeForPackage(context: Context, packageName: String): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE),1,0,0)
        val start = calendar.timeInMillis
        val end = System.currentTimeMillis()
        val stats: Map<String, UsageStats> =
            usageStatsManager.queryAndAggregateUsageStats(start, end)

        val usageStats = stats[packageName]
        return usageStats?.totalTimeInForeground ?: 0L
    }

    fun formatScreenTime(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    fun getAllInstalledApps(packageManager: PackageManager): MutableList<ResolveInfo> {
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

    fun getAppsListAlignmentFromPreferences(preferences: SharedPreferences): Alignment.Horizontal {
        return when (preferences.getString("AppsAlignment", "Center")) {
            "Center" -> Alignment.CenterHorizontally
            "Left" -> Alignment.Start
            else -> Alignment.End
        }
    }

    fun getResolveInfoFromPackageName(
        packageName: String,
        packageManager: PackageManager
    ): ResolveInfo? {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            return packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return null
    }

    fun getCurrentTime(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm") // Format as hours:minutes:seconds
        return now.format(formatter)
    }
}