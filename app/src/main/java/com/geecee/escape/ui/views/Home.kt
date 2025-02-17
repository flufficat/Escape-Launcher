package com.geecee.escape.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escape.R
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.AppUtils.getCurrentTime
import com.geecee.escape.utils.AppUtils.resetHome
import com.geecee.escape.utils.WidgetsScreen
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.getStringSetting
import com.geecee.escape.utils.getWidgetHeight
import com.geecee.escape.utils.getWidgetOffset
import com.geecee.escape.utils.getWidgetWidth
import com.geecee.escape.utils.managers.getTotalUsageForDate
import com.geecee.escape.utils.managers.getUsageForApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.geecee.escape.MainAppViewModel as MainAppModel

// Home Screen Page Inside the Pager
@Composable
fun HomeScreen(
    mainAppModel: MainAppModel, homeScreenModel: HomeScreenModel
) {
    val scrollState = rememberLazyListState()
    val noApps = remember { mutableStateOf(true) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = getHomeScreenVerticalArrangement(
            homeScreenModel.sharedPreferences,
            stringResource(R.string.HomeVAlignment)
        ),
        horizontalAlignment = getHomeScreenHorizontalArrangement(
            homeScreenModel.sharedPreferences,
            stringResource(R.string.HomeAlignment)
        ),
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 0.dp)
    ) {
        item {
            Spacer(Modifier.height(90.dp))
        }

        //Clock
        item {
            if (getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ShowClock), true
                )
            ) {
                Clock(homeScreenModel.sharedPreferences, mainAppModel.getContext(), noApps)
            }
        }

        //Screen time
        item {
            if (getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnHome), false
                )
            ) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayUsage = remember { mutableLongStateOf(0L) }

                LaunchedEffect(
                    mainAppModel.shouldReloadTotalScreenTimeOnHomeScreen.value
                ) {
                    try {
                        withContext(Dispatchers.IO) {
                            val usage = getTotalUsageForDate(today)
                            withContext(Dispatchers.Main) {
                                todayUsage.longValue = usage
                            }
                            mainAppModel.shouldReloadTotalScreenTimeOnHomeScreen.value = false
                        }
                    } catch (e: Exception) {
                        Log.e("ScreenTime", "Error fetching total usage: ${e.message}")
                    }
                }

                HomeScreenScreenTime(
                    AppUtils.formatScreenTime(todayUsage.longValue),
                    homeScreenModel.sharedPreferences
                )
            }
        }

        //Widgets
        item {
            var widgetOffset by remember { mutableIntStateOf(0) }
            widgetOffset = if (getStringSetting(
                    mainAppModel.getContext(), "HomeAlignment", "Center"
                ) == "Left"
            ) {
                -8
            } else if (getStringSetting(
                    mainAppModel.getContext(), "HomeAlignment", "Center"
                ) == "Right"
            ) {
                8
            } else {
                0
            }
            widgetOffset += getWidgetOffset(mainAppModel.getContext()).toInt()

            WidgetsScreen(context = mainAppModel.getContext(), modifier = Modifier
                .offset {
                    IntOffset(
                        (widgetOffset.dp)
                            .toPx()
                            .toInt(), 0
                    )
                }
                .size(
                    (getWidgetWidth(mainAppModel.getContext())).dp,
                    (getWidgetHeight(mainAppModel.getContext())).dp
                )
                .padding(0.dp, 7.dp))
        }

        //Apps
        items(homeScreenModel.favoriteApps) { app ->
            val appScreenTime = remember { mutableLongStateOf(0L) }

            // Fetch screen time in a coroutine
            LaunchedEffect(mainAppModel.shouldReloadAppUsageOnHome.value) {
                withContext(Dispatchers.IO) {
                    appScreenTime.longValue = getUsageForApp(
                        app, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )
                    mainAppModel.shouldReloadAppUsageOnHome.value = false
                }
            }

            HomeScreenItem(appName = AppUtils.getAppNameFromPackageName(
                context = mainAppModel.getContext(), packageName = app
            ), screenTime = appScreenTime.longValue, onAppClick = {
                homeScreenModel.currentPackageName.value = app

                AppUtils.openApp(
                    app, false, homeScreenModel.showOpenChallenge, mainAppModel
                )

                resetHome(homeScreenModel, mainAppModel)
            }, onAppLongClick = {
                homeScreenModel.showBottomSheet.value = true
                homeScreenModel.currentSelectedApp.value = AppUtils.getAppNameFromPackageName(
                    mainAppModel.getContext(), app
                )
                homeScreenModel.currentPackageName.value = app
                homeScreenModel.isCurrentAppChallenged.value =
                    mainAppModel.challengesManager.doesAppHaveChallenge(
                        app
                    )
                homeScreenModel.isCurrentAppHidden.value =
                    mainAppModel.hiddenAppsManager.isAppHidden(
                        app
                    )
                homeScreenModel.isCurrentAppFavorite.value =
                    mainAppModel.favoriteAppsManager.isAppFavorite(
                        app
                    )
                if (getBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.Haptic),
                        true
                    )
                ) {
                    homeScreenModel.haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }, showScreenTime = getBooleanSetting(
                mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnApp)
            ), modifier = Modifier
            )
        }

        if (getBooleanSetting(
                mainAppModel.getContext(),
                mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                true
            )
        ) {
            item {
                Spacer(Modifier.height(15.dp))
            }

            item {
                FirstTimeHelp()
            }
        }

        item {
            Spacer(Modifier.height(90.dp))
        }
    }
}

// Home Screen Clock
@Composable
fun Clock(
    sharedPreferencesSettings: SharedPreferences, context: Context, noApps: MutableState<Boolean>
) {
    var time by remember { mutableStateOf(getCurrentTime()) }
    val parts = time.split(":")
    val hours = parts[0]
    val minutes = parts[1]

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime()
            delay(1000) // Update every second
        }
    }

    if (getBooleanSetting(
            context = context, stringResource(R.string.BigClock), false
        )
    ) {
        Column {
            Text(
                text = hours,
                modifier = Modifier.offset(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )
            Text(
                text = minutes,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                modifier = Modifier.offset(0.dp)
            )
        }
    } else {
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = if (sharedPreferencesSettings.getString(
                    stringResource(R.string.HomeAlignment), "Center"
                ) == "Left"
            ) TextAlign.Start else if (sharedPreferencesSettings.getString(
                    stringResource(R.string.HomeAlignment), "Center"
                ) == "Right"
            ) TextAlign.End else TextAlign.Center
        )
    }
}

// Screen time on home screen
@Composable
fun HomeScreenScreenTime(
    screenTime: String, sharedPreferencesSettings: SharedPreferences
) {
    val alignModifier = if (sharedPreferencesSettings.getString(
            stringResource(R.string.HomeAlignment), "Center"
        ) == "Left"
    ) Modifier.offset((0).dp) else if (sharedPreferencesSettings.getString(
            stringResource(R.string.HomeAlignment), "Center"
        ) == "Right"
    ) Modifier.offset(0.dp) else Modifier.offset(0.dp)

    Text(
        text = screenTime,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = alignModifier.alpha(0.5f)
    )
}

@Composable
fun FirstTimeHelp() {
    Box(
        Modifier.clip(
            MaterialTheme.shapes.extraLarge
        )
    ) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier
                    .padding(25.dp, 25.dp, 25.dp, 15.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(25.dp, 0.dp, 25.dp, 25.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun getHomeScreenVerticalArrangement(
    sharedPreferences: SharedPreferences,
    preference: String
): Arrangement.Vertical {
    return when {
        sharedPreferences.getString(preference, "Center") == "Center" -> Arrangement.Center
        sharedPreferences.getString(preference, "Center") == "Top" -> Arrangement.Top
        else -> Arrangement.Bottom
    }
}


fun getHomeScreenHorizontalArrangement(
    sharedPreferences: SharedPreferences,
    preference: String
): Alignment.Horizontal {
    return when {
        sharedPreferences.getString(
            preference,
            "Center"
        ) == "Center" -> Alignment.CenterHorizontally

        sharedPreferences.getString(preference, "Center") == "Left" -> Alignment.Start
        else -> Alignment.End
    }
}