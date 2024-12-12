@file:Suppress("unused")

package com.geecee.escape.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import com.geecee.escape.MainHomeScreen
import com.geecee.escape.R

fun changeLauncher(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

fun changeTheme(theme: Int, context: Context, activity: Activity) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putInt(context.resources.getString(R.string.Theme), theme)

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    context.startActivity(intent, options.toBundle())
    activity.finish()
}

fun changeHomeAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(
        context.resources.getString(R.string.HomeAlignment), when (alignment) {
            1 -> "Center"
            0 -> "Left"
            else -> "Right"
        }
    )

    editor.apply()
}

fun getHomeAlignment(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(context.resources.getString(R.string.HomeAlignment), "Center") == "Left") {
        0
    } else if (sharedPreferences.getString(context.resources.getString(R.string.HomeAlignment), "Center") == "Center") {
        1
    } else {
        2
    }
}

fun changeHomeVAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(
        context.resources.getString(R.string.HomeVAlignment), when (alignment) {
            1 -> "Center"
            0 -> "Top"
            else -> "Bottom"
        }
    )

    editor.apply()
}

fun getHomeVAlignment(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(context.resources.getString(R.string.HomeVAlignment), "Center") == "Top") {
        0
    } else if (sharedPreferences.getString(context.resources.getString(R.string.HomeVAlignment), "Center") == "Center") {
        1
    } else {
        2
    }
}

fun changeAppsAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(
        context.resources.getString(R.string.AppsAlignment), when (alignment) {
            1 -> "Center"
            0 -> "Left"
            else -> "Right"
        }
    )

    editor.apply()
}

fun getAppsAlignment(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(context.resources.getString(R.string.AppsAlignment), "Center") == "Left") {
        0
    } else if (sharedPreferences.getString(context.resources.getString(R.string.AppsAlignment), "Center") == "Center") {
        1
    } else {
        2
    }
}

// Generic functions

fun toggleBooleanSetting(context: Context, shouldTurnOn: Boolean, setting: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putBoolean(setting, true)
    } else {
        editor.putBoolean(setting, false)
    }

    editor.apply()
}

fun getBooleanSetting(context: Context,setting:String): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getBoolean(setting,false)
}

fun getStringSetting(context: Context, setting: String, defaultValue: String): String {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getString(setting, defaultValue) ?: defaultValue
}

fun setStringSetting(context: Context, setting: String, value: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putString(setting, value)
    editor.apply()
}

fun getIntSetting(context: Context, setting: String, defaultValue: Int): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getInt(setting, defaultValue)
}

fun setIntSetting(context: Context, setting: String, value: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putInt(setting, value)
    editor.apply()
}

fun getBooleanSetting(context: Context, setting: String, defaultValue: Boolean): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getBoolean(setting, defaultValue)
}

fun setBooleanSetting(context: Context, setting: String, value: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putBoolean(setting, value)
    editor.apply()
}

fun resetActivity(context: Context, activity: Activity){
    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    context.startActivity(intent, options.toBundle())
    activity.finish()
}