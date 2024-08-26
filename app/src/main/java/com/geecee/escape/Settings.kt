package com.geecee.escape

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsScreen(context: Context, goHome: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    var homeAlignText by remember {
        mutableStateOf("Center")
    }

    homeAlignText = if (sharedPreferences.getString("HomeAlignment", "Center") == "Right") {
        "Left"
    } else if (sharedPreferences.getString("HomeAlignment", "Center") == "Center") {
        "Right"
    } else {
        "Center"
    }

    var homeVAlignText by remember {
        mutableStateOf("Center")
    }

    homeVAlignText = if (sharedPreferences.getString("HomeVAlignment", "Center") == "Top") {
        "Center"
    } else if (sharedPreferences.getString("HomeVAlignment", "Center") == "Center") {
        "Bottom"
    } else {
        "Top"
    }

    var appsAlignText by remember {
        mutableStateOf("Center")
    }

    appsAlignText = if (sharedPreferences.getString("AppsAlignment", "Center") == "Right") {
        "Left"
    } else if (sharedPreferences.getString("AppsAlignment", "Center") == "Center") {
        "Right"
    } else {
        "Center"
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

            Text(
                text = "Settings", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Change Background Colour",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = { changeWidget(context,goHome) },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Select Home Screen Widget",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = { toggleWidgets(context); goHome()},
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Toggle Widgets",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = { changeHomeAlignment(context); goHome() },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Align Home $homeAlignText",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = { changeAppsAlignment(context); goHome() },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Align Apps List $appsAlignText",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = { changeHomeVAlignment(context); goHome() },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Vertically Align Home $homeVAlignText",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Hidden Apps", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Open Challenges", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Font", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Make Default Launcher",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
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

fun toggleWidgets(context: Context){
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    if (sharedPreferences.getString("WidgetsToggle", "False") == "False"){
        editor.putString("WidgetsToggle", "True")
    }
    else{
        editor.putString("WidgetsToggle", "False")
    }

    editor.apply()
}

fun changeWidget(context: Context, goHome: () -> Unit){
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    removeWidget(context)


    if (sharedPreferences.getString("WidgetsToggle", "False") == "False"){
        editor.putString("WidgetsToggle", "True")
    }
    editor.apply()

    goHome()
}