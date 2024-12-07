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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.geecee.escape.R
import com.geecee.escape.utils.getStringSetting

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


private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@Composable
fun EscapeTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    // Set theme
    when (sharedPreferencesSettings.getInt("Theme", 0)) {
        0 -> {
            colorScheme = darkScheme
        }

        1 -> {
            colorScheme = lightScheme
        }

        2 -> {
            colorScheme = PitchDarkColorScheme
        }

        3 -> {
            colorScheme =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(context)
                } else {
                    darkScheme
                }
        }

        4 -> {
            colorScheme =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicLightColorScheme(context)
                } else {
                    lightScheme
                }

        }

        else -> {
            colorScheme = darkScheme
        }
    }

    // Make the typography
    val fontFamily = remember {
        mutableStateOf(
            FontFamily(
                Font(
                    googleFont = GoogleFont(getStringSetting(context,"font", "Jost"), true),
                    fontProvider = provider
                )
            )
        )
    }

    val typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 29.sp,
            letterSpacing = 0.6.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 25.sp,
            letterSpacing = 0.6.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.6.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 52.sp,
            lineHeight = 53.sp,
            letterSpacing = 0.6.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 48.sp,
            lineHeight = 49.sp,
            letterSpacing = 0.6.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily.value,
            fontWeight = FontWeight.Light,
            fontSize = 44.sp,
            lineHeight = 45.sp,
            letterSpacing = 0.6.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}