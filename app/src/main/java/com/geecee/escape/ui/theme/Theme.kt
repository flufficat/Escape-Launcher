package com.geecee.escape.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.geecee.escape.R

val DarkColorScheme = darkColorScheme(
    primary = primary,
    secondary = secondary,
    background = background,
    onPrimary = secondary,
)

val PitchDarkColorScheme = darkColorScheme(
    primary = primary,
    secondary = secondary,
    background = pitchBlackBackground,
    onPrimary = secondary,
)

val LightColorScheme = lightColorScheme(
    primary = lightPrimary,
    secondary = lightSecondary,
    background = lightBackground,
    onPrimary = lightSecondary,
)

@Composable
fun EscapeTheme(
    content: @Composable () -> Unit
) {
    val type: Typography
    Locale.current
    val context = LocalContext.current
    val colorScheme: ColorScheme
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    type = if (sharedPreferencesSettings.getString("font", "jost") == "jost") {
        JostTypography
    } else if (sharedPreferencesSettings.getString("font", "jost") == "lexend") {
        LexendTypography
    } else if (sharedPreferencesSettings.getString("font", "jost") == "inter") {
        InterTypography
    } else if (sharedPreferencesSettings.getString("font", "jost") == "work") {
        WorkTypography
    } else {
        JostTypography
    }

    when (sharedPreferencesSettings.getInt("Theme", 0)) {
        0 -> {
            colorScheme = DarkColorScheme
        }

        1 -> {
            colorScheme = LightColorScheme
        }

        2 -> {
            colorScheme = PitchDarkColorScheme
        }

        3 -> {
            colorScheme =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context)
            } else {
                DarkColorScheme
            }
        }

        4 -> {
            colorScheme =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                lightColorScheme()
            }

        }

        else -> {
            colorScheme = DarkColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = type,
        content = content
    )

}