package com.geecee.escape

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.geecee.escape.ui.theme.JostTypography


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(context: Context, goHome: () -> Unit, onOpenHiddenApps: () -> Unit, activity: Activity, onOpenChallenges: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    var homeAlignText by remember {
        mutableStateOf("Center")
    }

    homeAlignText = if (sharedPreferences.getString("HomeAlignment", "Center") == "Right") {
        stringResource(id = R.string.left)
    } else if (sharedPreferences.getString("HomeAlignment", "Center") == "Center") {
        stringResource(id = R.string.right)
    } else {
        stringResource(id = R.string.center)
    }

    var homeVAlignText by remember {
        mutableStateOf("Center")
    }

    homeVAlignText = if (sharedPreferences.getString("HomeVAlignment", "Center") == "Top") {
        stringResource(id = R.string.center)
    } else if (sharedPreferences.getString("HomeVAlignment", "Center") == "Center") {
        stringResource(id = R.string.bottom)
    } else {
        stringResource(id = R.string.top)
    }

    var appsAlignText by remember {
        mutableStateOf("Center")
    }

    appsAlignText = if (sharedPreferences.getString("AppsAlignment", "Center") == "Right") {
        stringResource(id = R.string.left)
    } else if (sharedPreferences.getString("AppsAlignment", "Center") == "Center") {
        stringResource(id = R.string.right)
    } else {
        stringResource(id = R.string.center)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(10.dp, 50.dp, 10.dp, 11.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Row(
                modifier = Modifier.combinedClickable(onClick = {
                    goHome()
                })
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go Back",tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                        .size(48.dp)
                        .fillMaxSize()
                        .align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(id = R.string.settings), color = MaterialTheme.colorScheme.primary, style = JostTypography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsButton(
                onClick = { toggleLightTheme(context, activity) },
                text = stringResource(R.string.toggle_light_mode)
            )

            SettingsButton(
                onClick = { changeWidget(context, goHome) },
                text = stringResource(R.string.select_home_screen_widget)
            )

            SettingsButton(
                onClick = { toggleWidgets(context); goHome() },
                text = stringResource(R.string.toggle_widgets)
            )

            SettingsButton(
                onClick = { changeHomeAlignment(context); goHome() },
                text = stringResource(R.string.align_home) + " " + homeAlignText
            )

            SettingsButton(
                onClick = { changeAppsAlignment(context); goHome() },
                text = stringResource(R.string.align_apps_list) + " " + appsAlignText
            )

            SettingsButton(
                onClick = { changeHomeVAlignment(context); goHome() },
                text = stringResource(R.string.vertically_align_home) + " " + homeVAlignText
            )

            SettingsButton(
                onClick = { onOpenHiddenApps() },
                text = stringResource(R.string.manage_hidden_apps)
            )

            SettingsButton(
                onClick = { onOpenChallenges() },
                text = stringResource(R.string.manage_open_challenges)
            )

            SettingsButton(
                onClick = { changeFont(context,activity)},
                text = stringResource(R.string.change_font)
            )

            SettingsButton(
                onClick = { changeLauncher(context) },
                text = stringResource(R.string.make_default_launcher)
            )


            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

fun toggleLightTheme(context: Context,activity: Activity){
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("LightMode", "False") == "False") {
        editor.putString("LightMode", "True")
    }
    else{
        editor.putString("LightMode", "False")
    }

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context,intent, options.toBundle())
    activity.finish()
}

fun changeWidget(context: Context, goHome: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    removeWidget(context)


    if (sharedPreferences.getString("WidgetsToggle", "False") == "False") {
        editor.putString("WidgetsToggle", "True")
    }
    editor.apply()

    goHome()
}

fun toggleWidgets(context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("WidgetsToggle", "False") == "False") {
        editor.putString("WidgetsToggle", "True")
    } else {
        editor.putString("WidgetsToggle", "False")
    }

    editor.apply()
}

fun changeHomeAlignment(context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("HomeAlignment", "Center") == "Left") {
        editor.putString("HomeAlignment", "Center")
    } else if (sharedPreferences.getString("HomeAlignment", "Center") == "Center") {
        editor.putString("HomeAlignment", "Right")
    } else {
        editor.putString("HomeAlignment", "Left")
    }

    editor.apply()
}

fun changeAppsAlignment(context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("AppsAlignment", "Center") == "Left") {
        editor.putString("AppsAlignment", "Center")
    } else if (sharedPreferences.getString("AppsAlignment", "Center") == "Center") {
        editor.putString("AppsAlignment", "Right")
    } else {
        editor.putString("AppsAlignment", "Left")
    }

    editor.apply()
}

fun changeHomeVAlignment(context: Context) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("HomeVAlignment", "Center") == "Top") {
        editor.putString("HomeVAlignment", "Center")
    } else if (sharedPreferences.getString("HomeVAlignment", "Center") == "Center") {
        editor.putString("HomeVAlignment", "Bottom")
    } else {
        editor.putString("HomeVAlignment", "Top")
    }

    editor.apply()
}

fun changeFont(context: Context, activity: Activity){
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("font", "jost") == "jost") {
        editor.putString("font", "lora")
    }
    else if (sharedPreferences.getString("font","jost") == "lora"){
        editor.putString("font", "josefin")
    }
    else if(sharedPreferences.getString("font","jost") == "josefin"){
        editor.putString("font", "jost")
    }
    else{
        editor.putString("font", "jost")
    }

    editor.apply()

    val intent = Intent(context, MainHomeScreen::class.java)
    val options = ActivityOptions.makeBasic()
    startActivity(context,intent, options.toBundle())
    activity.finish()
}

fun changeLauncher(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun SettingsButton(onClick: () -> Unit, text: String) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.padding(0.dp,0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text,
            Modifier.padding(0.dp,15.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }

}