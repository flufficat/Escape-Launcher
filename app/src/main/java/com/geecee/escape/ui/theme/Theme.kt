package com.geecee.escape.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.geecee.escape.R

private val DarkColorScheme = darkColorScheme(
    primary = primary,
    secondary = secondary,
    background = background
)

private val LightColorScheme = lightColorScheme(
    primary = lprimary,
    secondary = lsecondary,
    background = lbackground
)

@Composable
fun EscapeTheme(
    content: @Composable () -> Unit
) {
    var colorScheme = DarkColorScheme
    val type: Typography
    val locale = Locale.current
    val context = LocalContext.current
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    type = if (locale.language == "ja") {
        //Make font a font that supports japanese
        JPTypography
    } else {
        //Find users preferred font and use it here
        if (sharedPreferencesSettings.getString("font", "jost") == "jost") {
            JostTypography
        }
        else if (sharedPreferencesSettings.getString("font","jost") == "lora"){
            LoraTypography
        }
        else if(sharedPreferencesSettings.getString("font","jost") == "josefin"){
            JosefinTypography
        }
        else{
            JostTypography
        }
    }

    if(sharedPreferencesSettings.getString("LightMode", "False") == "True"){
        colorScheme = LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = type,
        content = content
    )

}