package com.geecee.escapelauncher.utils

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import com.geecee.escapelauncher.R

@Composable
fun WidgetsScreen(
    context: Context,
    modifier: Modifier
) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost = remember { AppWidgetHost(context, 1) }
    val appWidgetId by remember { mutableIntStateOf(getSavedWidgetId(context)) }
    var appWidgetHostView by remember { mutableStateOf<AppWidgetHostView?>(null) }


    // On appWidgetId change, re-setup the widget view
    LaunchedEffect(appWidgetId) {
        try {
            if (appWidgetId != -1) {
                val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (widgetInfo != null) {
                    appWidgetHostView =
                        appWidgetHost.createView(context, appWidgetId, widgetInfo).apply {
                            setAppWidget(appWidgetId, widgetInfo)
                        }
                } else {
                    Log.e("WidgetsScreen", "Widget info not found for ID $appWidgetId")
                }
            }
        } catch (e: Exception) {
            Log.e("Widget error", e.message.toString())
        }
    }

    appWidgetHostView?.let { hostView ->
        AndroidView(
            factory = { hostView },
            modifier = modifier
        )
    }
}

fun openWidgetPicker(
    appWidgetHost: AppWidgetHost,
    widgetPickerLauncher: ActivityResultLauncher<Intent>
) {
    val appWidgetId = appWidgetHost.allocateAppWidgetId()

// Add a "None" option to the picker
    val customOptions = arrayListOf<Bundle>().apply {
        add(Bundle().apply {
            putString("custom_option_key", "None")
        })
    }

    val customExtras = arrayListOf<Bundle>().apply {
        add(Bundle().apply {
            putString("custom_option_key", "None")
        })
    }

    val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customOptions)
        putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)

    }

// Launch the widget picker
    widgetPickerLauncher.launch(pickIntent)
}

fun launchWidgetConfiguration(context: Context, appWidgetId: Int) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
    val configureComponent = appWidgetInfo?.configure

    configureComponent?.let {
        val resolveInfo = context.packageManager.resolveActivity(
            Intent().setComponent(configureComponent),
            PackageManager.MATCH_DEFAULT_ONLY
        )

        if (resolveInfo?.activityInfo?.exported == true) {
            val configureIntent = Intent().apply {
                component = it
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this flag
            }
            context.startActivity(configureIntent)
        } else {
            Log.w("WidgetsScreen", "Configuration activity is not exported and cannot be started.")
        }
    }
}

fun isWidgetConfigurable(context: Context, appWidgetId: Int): Boolean {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return false
    val configureComponent = appWidgetInfo.configure

    // Check if the configuration activity is exported
    val resolveInfo = context.packageManager.resolveActivity(
        Intent().setComponent(configureComponent),
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return resolveInfo != null && resolveInfo.activityInfo.exported
}

fun saveWidgetId(context: Context, widgetId: Int) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putInt("widget_id", widgetId)
    }
}

fun getSavedWidgetId(context: Context): Int {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("widget_id", -1)
}

fun removeWidget(context: Context) {
    // Save the updated widget ID to shared preferences
    saveWidgetId(context, -1)
}

fun setWidgetOffset(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetOffset", sliderPosition)

    }
}

fun getWidgetOffset(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetOffset", 0f)
}

fun setWidgetHeight(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetHeight", sliderPosition)

    }
}

fun getWidgetWidth(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetWidth", 150f)
}

fun setWidgetWidth(context: Context, sliderPosition: Float) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putFloat("WidgetWidth", sliderPosition)

    }
}

fun getWidgetHeight(context: Context): Float {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getFloat("WidgetHeight", 125f)
}
