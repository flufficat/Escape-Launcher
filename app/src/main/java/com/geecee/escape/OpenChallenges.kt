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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "ChallengePrefs"
        private const val FAVORITE_APPS_KEY = "ChallengeApps"
    }

    private fun saveChallengeApps(challengeApps: List<String>) {
        val json = gson.toJson(challengeApps)
        with(sharedPreferences.edit()) {
            putString(FAVORITE_APPS_KEY, json)
            apply()
        }
    }

    fun getChallengeApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addChallengeApp(packageName: String) {
        val challengeApps = getChallengeApps().toMutableList()
        if (challengeApps.size < 5 && packageName !in challengeApps) {
            challengeApps.add(packageName)
            saveChallengeApps(challengeApps)
        }
    }

    fun removeChallengeApp(packageName: String) {
        val challengeApps = getChallengeApps().toMutableList()
        if (challengeApps.remove(packageName)) {
            saveChallengeApps(challengeApps)
        }
    }

    fun doesAppHaveChallenge(packageName: String): Boolean {
        val challengeApps = getChallengeApps()
        return packageName in challengeApps
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChallengeAppsScreen(
    context: Context,
    challengesManager: ChallengesManager,
    packageManager: PackageManager,
    goBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val challengeApps = remember { mutableStateOf(challengesManager.getChallengeApps()) }

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
                        .size(48.dp)
                        .fillMaxSize()
                        .align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.open_challenges),
                    fontSize = 38.sp,

                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = stringResource(id = R.string.long_press_to_remove_challenge),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )


            Spacer(modifier = Modifier.height(16.dp))

            for (app in challengeApps.value) {
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
                            challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = challengesManager.getChallengeApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}