//Thanks ChatGPT ;)

package com.geecee.escape

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoriteAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "FavoriteAppsPrefs"
        private const val FAVORITE_APPS_KEY = "FavoriteApps"
    }

    private fun saveFavoriteApps(favoriteApps: List<String>) {
        val json = gson.toJson(favoriteApps)
        with(sharedPreferences.edit()) {
            putString(FAVORITE_APPS_KEY, json)
            apply()
        }
    }

    fun getFavoriteApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addFavoriteApp(packageName: String) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (favoriteApps.size < 5 && packageName !in favoriteApps) {
            favoriteApps.add(packageName)
            saveFavoriteApps(favoriteApps)
        }
    }

    fun removeFavoriteApp(packageName: String) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (favoriteApps.remove(packageName)) {
            saveFavoriteApps(favoriteApps)
        }
    }

    fun isAppFavorite(packageName: String): Boolean {
        val favoriteApps = getFavoriteApps()
        return packageName in favoriteApps
    }
}

