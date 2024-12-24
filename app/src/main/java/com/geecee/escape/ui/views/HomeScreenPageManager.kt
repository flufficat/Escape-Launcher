package com.geecee.escape.ui.views

import android.content.Context
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val installedApps: MutableList<ResolveInfo>, // List of installed apps
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
    onOpenSettings: () -> Unit
) {
    //Set up variables
    val homeScreenModel = HomeScreenModel(
        showBottomSheet = remember { mutableStateOf(false) },
        sheetState = rememberModalBottomSheetState(),
        currentSelectedApp = remember { mutableStateOf("") },
        currentPackageName = remember { mutableStateOf("") },
        isCurrentAppFavorite = remember { mutableStateOf(false) },
        isCurrentAppChallenged = remember { mutableStateOf(false) },
        isCurrentAppHidden = remember { mutableStateOf(false) },
        haptics = LocalHapticFeedback.current,
        sharedPreferences = mainAppModel.getContext().getSharedPreferences(
            R.string.settings_pref_file_name.toString(),
            Context.MODE_PRIVATE
        ),
        favoriteApps = remember { mutableStateListOf<String>().apply { addAll(mainAppModel.favoriteAppsManager.getFavoriteApps()) } },
        interactionSource = remember { MutableInteractionSource() },
        showOpenChallenge = remember { mutableStateOf(false) },
        pagerState = rememberPagerState(1, 0f) { 3 },
        coroutineScope = rememberCoroutineScope(),
        installedApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager),
        sortedInstalledApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager)
            .sortedBy {
                AppUtils.getAppNameFromPackageName(
                    mainAppModel.getContext(),
                    it.activityInfo.packageName
                )
            },
        appsListScrollState = rememberLazyListState(),
        searchText = remember { mutableStateOf("") },
        searchExpanded = remember { mutableStateOf(false) },
        showPrivateSpaceSettings = remember { mutableStateOf(false) }
    )

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
            0 -> ScreenTimeDashboard()

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
                    } else {
                        mainAppModel.favoriteAppsManager.addFavoriteApp(
                            homeScreenModel.currentPackageName.value
                        )
                        homeScreenModel.isCurrentAppFavorite.value = true
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
        OpenChallenge({
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
        modifier = modifier
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier
                .padding(vertical = 15.dp)
                .combinedClickable(
                    onClick = onAppClick,
                    onLongClick = onAppLongClick
                ),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )

        // Optional screen time
        if (showScreenTime && screenTime != null) {
            Text(
                AppUtils.formatScreenTime(screenTime),
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 5.dp)
                    .alpha(0.5f)
                    .combinedClickable(
                        onClick = onAppClick,
                        onLongClick = onAppLongClick
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

}

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
                    Icons.Rounded.Settings,
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

data class AppAction(
    val label: String,
    val onClick: () -> Unit
)