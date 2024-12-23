package com.geecee.escape.ui.views

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.AppUtils.getCurrentTime
import com.geecee.escape.utils.AppUtils.resetHome
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.managers.getUsageForApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Home Screen Page Inside the Pager
@Composable
fun HomeScreen(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel
) {
    val scrollState = rememberLazyListState()
    val noApps = remember { mutableStateOf(true) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = if (homeScreenModel.sharedPreferences.getString(
                stringResource(R.string.HomeVAlignment), "Center"
            ) == "Center"
        ) Arrangement.Center else if (homeScreenModel.sharedPreferences.getString(
                stringResource(R.string.HomeVAlignment), "Center"
            ) == "Top"
        ) Arrangement.Top else Arrangement.Bottom,
        horizontalAlignment = if (homeScreenModel.sharedPreferences.getString(
                stringResource(R.string.HomeAlignment), "Center"
            ) == "Center"
        ) Alignment.CenterHorizontally else if (homeScreenModel.sharedPreferences.getString(
                stringResource(R.string.HomeAlignment), "Center"
            ) == "Left"
        ) Alignment.Start else Alignment.End,
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 0.dp)
    ) {
        item {
            Spacer(Modifier.height(90.dp))
        }


        //Clock
        item {
            if (getBooleanSetting(mainAppModel.context, stringResource(R.string.ShowClock), true)) {
                Clock(homeScreenModel.sharedPreferences, mainAppModel.context, noApps)
            }
        }

        //Apps
        items(homeScreenModel.favoriteApps) { app ->
            val appScreenTime = remember { androidx.compose.runtime.mutableLongStateOf(0L) }

            // Fetch screen time in a coroutine
            LaunchedEffect(app) {
                withContext(Dispatchers.IO) {
                    appScreenTime.longValue = getUsageForApp(
                        app,
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )
                }
            }

            HomeScreenItem(
                appName = AppUtils.getAppNameFromPackageName(context = mainAppModel.context, packageName = app),
                screenTime = appScreenTime.longValue,
                onAppClick = {
                    homeScreenModel.currentPackageName.value = app

                    AppUtils.openApp(
                        app,
                        false,
                        homeScreenModel.showOpenChallenge,
                        mainAppModel
                    )

                    resetHome(homeScreenModel)
                },
                onAppLongClick = {
                    homeScreenModel.showBottomSheet.value = true
                    homeScreenModel.currentSelectedApp.value =
                        AppUtils.getAppNameFromPackageName(
                            mainAppModel.context,
                            app
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
                },
                showScreenTime = getBooleanSetting(
                    mainAppModel.context,
                    stringResource(R.string.screen_time_on_app)
                ),
                modifier = Modifier
            )
        }

        if (getBooleanSetting(
                mainAppModel.context,
                mainAppModel.context.resources.getString(R.string.FirstTimeAppDrawHelp),
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
    sharedPreferencesSettings: SharedPreferences,
    context: Context,
    noApps: MutableState<Boolean>
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
            context = context,
            stringResource(R.string.BigClock),
            false
        )
    ) {
        Column {
            Text(
                text = hours,
                modifier = Modifier.offset(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )
            Text(
                text = minutes,
                color = MaterialTheme.colorScheme.onBackground,
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
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = if (sharedPreferencesSettings.getString(
                    stringResource(R.string.HomeAlignment),
                    "Center"
                ) == "Left"
            ) Modifier.offset((0).dp) else if (sharedPreferencesSettings.getString(
                    stringResource(R.string.HomeAlignment),
                    "Center"
                ) == "Right"
            ) Modifier.offset(0.dp) else Modifier.offset(0.dp)
        )
    }
}

@Composable
fun FirstTimeHelp(){
    Box(
        Modifier
            .clip(
                MaterialTheme.shapes.extraLarge
            )
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.padding(25.dp, 25.dp, 25.dp, 15.dp).align(Alignment.CenterHorizontally)) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(Modifier.padding(25.dp, 0.dp, 25.dp, 25.dp).align(Alignment.CenterHorizontally)) {
                Icon(
                    painterResource(R.drawable.radio_button_unchecked),
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}