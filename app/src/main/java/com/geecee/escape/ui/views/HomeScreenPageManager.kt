package com.geecee.escape.ui.views

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.R
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.AppUtils.updateFavorites
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.managers.OpenChallenge
import com.geecee.escape.utils.setBooleanSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.geecee.escape.MainAppViewModel as MainAppModel

// Model to be passed around home screen pages
data class HomeScreenModel @OptIn(ExperimentalMaterial3Api::class) constructor(
    var showBottomSheet: MutableState<Boolean>, // Whether or not the Bottom sheet should be shown
    var sheetState: SheetState, // The bottom sheet state
    var currentSelectedApp: MutableState<String>, // The currently selected app, set when pressed or long held, used for things like bottom sheet
    var currentPackageName: MutableState<String>, // Similar idea to currentSelectedApp
    var isCurrentAppFavorite: MutableState<Boolean>, // If the currentSelectedApp is a favourite
    var isCurrentAppChallenged: MutableState<Boolean>, // If the currentSelectedApp is challenged
    var isCurrentAppHidden: MutableState<Boolean>, // If the currentSelectedApp is hidden (don't know how you've managed to do that if it is lol)
    var haptics: HapticFeedback, // Haptic feedback (self explanatory)
    var sharedPreferences: SharedPreferences, // The sharedPreferences
    var favoriteApps: SnapshotStateList<String>, // List of apps that are favourites
    var interactionSource: MutableInteractionSource, // Something to do with clicking stuff idk
    var showOpenChallenge: MutableState<Boolean>, // Whether there is an open challenge open rn
    var pagerState: PagerState, // I wonder what this does
    var coroutineScope: CoroutineScope, // Same applies
    var installedApps: MutableList<ResolveInfo>, // List of installed apps
    var sortedInstalledApps: List<ResolveInfo>, // Sorted list of installed apps
    val appsListScrollState: LazyListState, // Scroll state of the apps list
    val searchText: MutableState<String>, // The current text in the search box
    val searchExpanded: MutableState<Boolean>, // If the search box is currently expanded
    val showPrivateSpaceSettings: MutableState<Boolean> // Whether the private space settings are open
)

// Main composable for home screen - contains a pager with all the pages inside of it
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun HomeScreenPageManager(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    onOpenSettings: () -> Unit
) {
    // Home Screen Pages
    HorizontalPager(
        state = homeScreenModel.pagerState,
        Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = {}.toString(),
                onLongClick = {
                    if (getBooleanSetting(
                            mainAppModel.getContext(),
                            mainAppModel.getContext().resources.getString(R.string.Haptic),
                            true
                        )
                    ) {
                        homeScreenModel.haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onOpenSettings()
                    setBooleanSetting(
                        mainAppModel.getContext(),
                        mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                        false
                    )
                },
                indication = null, interactionSource = homeScreenModel.interactionSource
            )
    ) { page ->
        when (page) {
            0 -> ScreenTimeDashboard(mainAppModel.getContext(), mainAppModel)

            1 -> HomeScreen(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel
            )

            2 -> AppsList(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel
            )
        }
    }


    //Bottom Sheet
    if (homeScreenModel.showBottomSheet.value) {
        var actions = listOf(
            AppAction(
                label = stringResource(id = R.string.uninstall),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DELETE,
                        Uri.parse("package:${homeScreenModel.currentPackageName.value}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainAppModel.getContext().startActivity(intent)
                }
            ),
            AppAction(
                label = stringResource(if (homeScreenModel.isCurrentAppFavorite.value) R.string.rem_from_fav else R.string.add_to_fav),
                onClick = {
                    if (homeScreenModel.isCurrentAppFavorite.value) {
                        mainAppModel.favoriteAppsManager.removeFavoriteApp(
                            homeScreenModel.currentPackageName.value
                        )
                        homeScreenModel.isCurrentAppFavorite.value = false
                        homeScreenModel.showBottomSheet.value = false
                    } else {
                        mainAppModel.favoriteAppsManager.addFavoriteApp(
                            homeScreenModel.currentPackageName.value
                        )
                        homeScreenModel.isCurrentAppFavorite.value = true
                        homeScreenModel.showBottomSheet.value = false
                        homeScreenModel.coroutineScope.launch {
                            homeScreenModel.pagerState.scrollToPage(1, 0f)
                        }
                    }
                    updateFavorites(mainAppModel, homeScreenModel.favoriteApps)
                }
            ),
            AppAction(
                label = stringResource(R.string.hide),
                onClick = {
                    mainAppModel.hiddenAppsManager.addHiddenApp(homeScreenModel.currentPackageName.value)
                    homeScreenModel.showBottomSheet.value = false
                }
            ),
            AppAction(
                label = stringResource(id = R.string.app_info),
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${homeScreenModel.currentPackageName.value}")
                    }.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    mainAppModel.getContext().startActivity(intent)
                }
            )
        )

        if (!homeScreenModel.isCurrentAppChallenged.value) {
            actions = actions +
                    AppAction(
                        label = stringResource(R.string.add_open_challenge),
                        onClick = {
                            mainAppModel.challengesManager.addChallengeApp(
                                homeScreenModel.currentPackageName.value
                            )
                            homeScreenModel.showBottomSheet.value = false
                            homeScreenModel.isCurrentAppChallenged.value = true
                        }
                    )
        }


        HomeScreenBottomSheet(
            title = homeScreenModel.currentSelectedApp.value,
            actions = actions,
            onDismissRequest = { homeScreenModel.showBottomSheet.value = false },
            sheetState = homeScreenModel.sheetState
        )
    }

    //Open Challenge
    AnimatedVisibility(
        visible = homeScreenModel.showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge(homeScreenModel.haptics, {
            AppUtils.openApp(
                homeScreenModel.currentPackageName.value,
                true,
                null,
                mainAppModel
            )
            homeScreenModel.showOpenChallenge.value = false
        }, {
            homeScreenModel.showOpenChallenge.value = false
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    appName: String,
    screenTime: Long? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    showScreenTime: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.combinedClickable(
            onClick = onAppClick,
            onLongClick = onAppLongClick
        )
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 15.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )

        // Optional screen time
        if (showScreenTime && screenTime != null) {
            Text(
                AppUtils.formatScreenTime(screenTime),
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .alpha(0.5f),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class AppAction(
    val label: String,
    val onClick: () -> Unit
) // Actions that can be shown in the bottom sheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenBottomSheet(
    title: String,
    actions: List<AppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 15.dp))

            // Actions
            Column(Modifier.padding(start = 47.dp)) {
                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .combinedClickable(onClick = action.onClick),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}