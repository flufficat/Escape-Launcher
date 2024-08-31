package com.geecee.escape

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity

fun changeWidget(context: Context, goHome: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    removeWidget(context)


    if (sharedPreferences.getString("WidgetsToggle", "False") == "False") {
        editor.putString("WidgetsToggle", "True")
    }
    editor.apply()

    goHome()
}

fun changeLauncher(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

fun toggleLightTheme(shouldTurnOn: Boolean,context: Context, activity: Activity) {
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

fun toggleSearchBox(shouldTurnOn: Boolean,context: Context) {
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

    return sharedPreferences.getString("showSearchBox", "False") == "True"
}

fun toggleWidgets(context: Context, shouldTurnOn: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (shouldTurnOn) {
        editor.putString("WidgetsToggle", "True")
    } else {
        editor.putString("WidgetsToggle", "False")
    }

    editor.apply()
}

fun getWidgetEnabled(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getString("WidgetsToggle", "False") == "True"
}

fun changeHomeAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (alignment == 1) {
        editor.putString("HomeAlignment", "Center")
    } else if (alignment == 0) {
        editor.putString("HomeAlignment", "Left")
    } else {
        editor.putString("HomeAlignment", "Right")
    }

    editor.apply()
}

fun getHomeAlignment(context: Context): Int{
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if(sharedPreferences.getString("HomeAlignment", "Center") == "Left"){
        0
    }
    else if (sharedPreferences.getString("HomeAlignment", "Center") == "Center"){
        1
    }
    else{
        2
    }
}

fun changeHomeVAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (alignment == 1) {
        editor.putString("HomeVAlignment", "Center")
    } else if (alignment == 0) {
        editor.putString("HomeVAlignment", "Top")
    } else {
        editor.putString("HomeVAlignment", "Bottom")
    }

    editor.apply()
}

fun getHomeVAlignment(context: Context): Int{
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if(sharedPreferences.getString("HomeVAlignment", "Center") == "Top"){
        0
    }
    else if (sharedPreferences.getString("HomeVAlignment", "Center") == "Center"){
        1
    }
    else{
        2
    }
}

fun changeAppsAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (alignment == 1) {
        editor.putString("AppsAlignment", "Center")
    } else if (alignment == 0) {
        editor.putString("AppsAlignment", "Left")
    } else {
        editor.putString("AppsAlignment", "Right")
    }

    editor.apply()
}

fun getAppsAlignment(context: Context): Int{
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if(sharedPreferences.getString("AppsAlignment", "Center") == "Left"){
        0
    }
    else if (sharedPreferences.getString("AppsAlignment", "Center") == "Center"){
        1
    }
    else{
        2
    }
}

fun changeFont(context: Context, activity: Activity, font: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (font == "lora") {
        editor.putString("font", "lora")
    } else if (font == "josefin") {
        editor.putString("font", "josefin")
    } else if (font == "jost") {
        editor.putString("font", "jost")
    } else {
        editor.putString("font", "jost")
    }

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context, intent, options.toBundle())
    activity.finish()
}

@Composable
fun SegmentedButtonGroup(
    buttons: List<String>,
    selectedButtonIndex: Int,
    onButtonClick: (Int) -> Unit
) {
    // Use the provided `selectedButtonIndex` directly for displaying the selected state
    var selectedIndex by remember { mutableIntStateOf(selectedButtonIndex) }

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
    ) {
        buttons.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                buttons.size - 1 -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                        shape = shape
                    )
                    .clickable {
                        selectedIndex = index
                        onButtonClick(index)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
