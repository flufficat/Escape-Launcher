package com.geecee.escape.ui.views

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import com.geecee.escape.MainAppModel
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
                "HomeVAlignment", "Center"
            ) == "Center"
        ) Arrangement.Center else if (homeScreenModel.sharedPreferences.getString(
                "HomeVAlignment", "Center"
            ) == "Top"
        ) Arrangement.Top else Arrangement.Bottom,
        horizontalAlignment = if (homeScreenModel.sharedPreferences.getString(
                "HomeAlignment", "Center"
            ) == "Center"
        ) Alignment.CenterHorizontally else if (homeScreenModel.sharedPreferences.getString(
                "HomeAlignment", "Center"
            ) == "Left"
        ) Alignment.Start else Alignment.End,
        modifier = modifier
    ) {
        item {
            if (getBooleanSetting(mainAppModel.context, "ShowClock", true)) {
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

    if (getBooleanSetting(context = mainAppModel.context,"BigClock",false)) {
        Column {
            Text(
                text = hours,
                modifier = Modifier.offset(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )
            Text(
                text = minutes,
                color = MaterialTheme.colorScheme.onBackground,
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
            modifier = if (sharedPreferencesSettings.getString(
                    "HomeAlignment",
                    "Center"
                ) == "Left"
            ) Modifier.offset((0).dp) else if (sharedPreferencesSettings.getString(
                    "HomeAlignment",
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