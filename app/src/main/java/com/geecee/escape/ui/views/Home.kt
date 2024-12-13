package com.geecee.escape.ui.views

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.geecee.escape.utils.getBooleanSetting
import kotlinx.coroutines.delay

// Home Screen Page Inside the Pager
@Composable
fun HomeScreen(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    modifier: Modifier,
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
        modifier = modifier
    ) {
        item {
            if (getBooleanSetting(mainAppModel.context, stringResource(R.string.ShowClock), true)) {
                Clock(homeScreenModel.sharedPreferences, mainAppModel, noApps)
            }
        }

        items(homeScreenModel.favoriteApps) { app ->
            AppsListItem(
                AppUtils.getResolveInfoFromPackageName(app, mainAppModel.packageManager),
                mainAppModel = mainAppModel,
                homeScreenModel,
                null,
                null,
                null,
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
        }
    }

}

// Home Screen Clock
@Composable
fun Clock(
    sharedPreferencesSettings: SharedPreferences,
    mainAppModel: MainAppModel,
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
            context = mainAppModel.context,
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

// Reloads favourite apps
fun updateFavorites(
    mainAppModel: MainAppModel,
    favoriteApps: SnapshotStateList<String>
) {
    favoriteApps.clear()
    favoriteApps.addAll(mainAppModel.favoriteAppsManager.getFavoriteApps())
}