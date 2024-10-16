package com.geecee.escape.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.geecee.escape.MainHomeScreen
import com.geecee.escape.R

fun changeLauncher(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

fun toggleLightTheme(shouldTurnOn: Boolean, context: Context, activity: Activity) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("LightMode", "True")
    } else {
        editor.putString("LightMode", "False")
    }

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context, intent, options.toBundle())
    activity.finish()
}

fun getLightTheme(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("LightMode", "False") == "True"
}

fun toggleSearchBox(shouldTurnOn: Boolean, context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("showSearchBox", "True")
    } else {
        editor.putString("showSearchBox", "False")
    }

    editor.apply()
}

fun getSearchBox(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("showSearchBox", "True") == "True"
}

fun changeHomeAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(
        "HomeAlignment", when (alignment) {
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

    return if (sharedPreferences.getString("HomeAlignment", "Center") == "Left") {
        0
    } else if (sharedPreferences.getString("HomeAlignment", "Center") == "Center") {
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
        "HomeVAlignment", when (alignment) {
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

    return if (sharedPreferences.getString("HomeVAlignment", "Center") == "Top") {
        0
    } else if (sharedPreferences.getString("HomeVAlignment", "Center") == "Center") {
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
        "AppsAlignment", when (alignment) {
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

    return if (sharedPreferences.getString("AppsAlignment", "Center") == "Left") {
        0
    } else if (sharedPreferences.getString("AppsAlignment", "Center") == "Center") {
        1
    } else {
        2
    }
}

fun changeFont(context: Context, activity: Activity, font: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    editor.putString(
        "font",font
    )

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context, intent, options.toBundle())
    activity.finish()
}

fun getAutoOpen(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("searchAutoOpen", "False") == "True"
}

fun toggleAutoOpen(context: Context, shouldTurnOn: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("searchAutoOpen", "True")
    } else {
        editor.putString("searchAutoOpen", "False")
    }

    editor.apply()
}

fun getDynamicColour(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("DynamicColour", "False") == "True"
}

fun toggleDynamicColour(context: Context, shouldTurnOn: Boolean, activity: Activity) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("DynamicColour", "True")
    } else {
        editor.putString("DynamicColour", "False")
    }

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context, intent, options.toBundle())
    activity.finish()
}

fun getClock(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("ShowClock", "True") == "True"
}

fun toggleClock(context: Context, shouldTurnOn: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("ShowClock", "True")
    } else {
        editor.putString("ShowClock", "False")
    }

    editor.apply()
}


fun getBigClock(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("BigClock", "False") == "True"
}

fun toggleBigClock(context: Context, shouldTurnOn: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("BigClock", "True")
    } else {
        editor.putString("BigClock", "False")
    }

    editor.apply()
}


// DevOptions

fun getFirstTime(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("FirstTime", "False") == "True"
}

fun resetFirstTime(context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor = sharedPreferences.edit()

    editor.putString("FirstTime", "True")
    editor.putString("hasDoneSetupPageOne", "False")

    editor.apply()
}