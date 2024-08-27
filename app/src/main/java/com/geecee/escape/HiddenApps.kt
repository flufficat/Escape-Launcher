package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HiddenAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "HiddenAppsPrefs"
        private const val FAVORITE_APPS_KEY = "HiddenApps"
    }

    private fun saveHiddenApps(hiddenApps: List<String>) {
        val json = gson.toJson(hiddenApps)
        with(sharedPreferences.edit()) {
            putString(FAVORITE_APPS_KEY, json)
            apply()
        }
    }

    fun getHiddenApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableList()
        if (hiddenApps.size < 5 && packageName !in hiddenApps) {
            hiddenApps.add(packageName)
            saveHiddenApps(hiddenApps)
        }
    }

    fun removeHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableList()
        if (hiddenApps.remove(packageName)) {
            saveHiddenApps(hiddenApps)
        }
    }

    fun isAppHidden(packageName: String): Boolean {
        val hiddenApps = getHiddenApps()
        return packageName in hiddenApps
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenAppsScreen(
    context: Context,
    hiddenAppsManager: HiddenAppsManager,
    packageManager: PackageManager,
    goBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val hiddenApps = remember { mutableStateOf(hiddenAppsManager.getHiddenApps()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(0.dp, 50.dp, 0.dp, 0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(30.dp, 0.dp, 30.dp, 140.dp)
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Row(
                modifier = Modifier.combinedClickable(onClick = {
                    goBack()
                })
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go Back",tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                        .size(48.dp).fillMaxSize().align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Hidden Apps", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Long press to un-hide",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )


            Spacer(modifier = Modifier.height(16.dp))

            for (app in hiddenApps.value) {
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
                            hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}