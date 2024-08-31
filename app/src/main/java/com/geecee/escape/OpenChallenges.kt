package com.geecee.escape

import android.content.Context
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
