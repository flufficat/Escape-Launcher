package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.views.HomeScreenModel
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

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
                mainAppModel.getContext().startActivity(launchIntent, options.toBundle())

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

    // Cache to store package name to app name mappings
    private val appNameCache = mutableMapOf<String, String>()

    fun getAppNameFromPackageName(context: Context, packageName: String): String {
        // Check cache first for instant return
        appNameCache[packageName]?.let { return it }
        
        // If not in cache, perform the operation directly but still cache the result
        try {
            val packageManager: PackageManager = context.packageManager
            val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            
            // Cache the result for future use
            appNameCache[packageName] = appName
            
            return appName
        } catch (e: PackageManager.NameNotFoundException) {
            return "null"
        }
    }

    fun filterAndSortApps(
        apps: List<ResolveInfo>,
        searchText: String,
        packageManager: PackageManager
    ): List<ResolveInfo> {
        return apps.map { appInfo ->
            appInfo to appInfo.loadLabel(packageManager).toString()
        }.filter { (_, appName) ->
            appName.contains(searchText, ignoreCase = true)
        }.sortedBy { (_, appName) -> appName }
            .map { (appInfo, _) -> appInfo }
    }

    fun getAppsListAlignmentFromPreferences(
        preferences: SharedPreferences,
        context: Context
    ): Alignment.Horizontal {
        return when (preferences.getString(
            context.resources.getString(R.string.AppsAlignment),
            "Center"
        )) {
            "Center" -> Alignment.CenterHorizontally
            "Left" -> Alignment.Start
            else -> Alignment.End
        }
    }

    fun getCurrentTime(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm") // Format as hours:minutes:seconds
        return now.format(formatter)
    }

    fun loadTextFromAssets(context: Context, fileName: String): String? {
        var inputStream: InputStream? = null
        var fileContent: String? = null
        try {
            inputStream = context.assets.open(fileName)
            fileContent = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return fileContent
    }

    fun isDefaultLauncher(context: Context): Boolean {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    fun resetHome(homeScreenModel: HomeScreenModel, mainAppModel: MainAppViewModel, shouldGoToFirstPage: Boolean? = true) {
        homeScreenModel.coroutineScope.launch {
            delay(200)
            if(shouldGoToFirstPage == true){
                homeScreenModel.pagerState.scrollToPage(1)
                homeScreenModel.appsListScrollState.scrollToItem(0)
            }
            homeScreenModel.searchExpanded.value = false
            homeScreenModel.searchText.value = ""
            homeScreenModel.installedApps =
                getAllInstalledApps(packageManager = mainAppModel.packageManager)
            homeScreenModel.sortedInstalledApps =
                getAllInstalledApps(packageManager = mainAppModel.packageManager)
                    .sortedBy {
                        // Use the non-suspending version with caching
                        getAppNameFromPackageName(
                            mainAppModel.getContext(),
                            it.activityInfo.packageName
                        )
                    }
        }
    }

    fun updateFavorites(
        mainAppModel: MainAppModel,
        favoriteApps: SnapshotStateList<String>
    ) {
        favoriteApps.clear()
        favoriteApps.addAll(mainAppModel.favoriteAppsManager.getFavoriteApps())
    }

    fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return yesterdayDate
    }

    fun doHapticFeedBack(context: Context, hapticFeedback: HapticFeedback){
        if (getBooleanSetting(
                context,
                context.resources.getString(R.string.Haptic),
                true
            )
        ) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}