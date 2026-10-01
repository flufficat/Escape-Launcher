package com.geecee.escapelauncher.feature.appslist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Main App List composable - focuses purely on the list of apps
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val haptics = LocalHapticFeedback.current
    val appUsageList by screenTimeViewModel.appUsageUiList.collectAsState()
    val showScreenTimeApp by appsListViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
    val hapticFeedbackEnabled by appsListViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val showWallpaper by appsListViewModel.showWallpaper.collectAsState(initial = false)
    val items by appsListViewModel.items.collectAsState()

    // Standard app interaction logic shared across slots
    val handleItemClick: (LauncherItem) -> Unit = { item ->
        when (item) {
            is LauncherItem.App -> {
                onAppOpened(item.app)
                appsListViewModel.onSearchExpandedChanged(false)
            }
            is LauncherItem.Shortcut -> appsListViewModel.openShortcut(item.shortcut)
        }
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    val handleItemLongClick: (LauncherItem) -> Unit = { item ->
        appsListViewModel.setBottomSheetVisible(true)
        appsListViewModel.setBottomSheetApp(item)
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    // Handle UI Events from ViewModel
    LaunchedEffect(Unit) {
        appsListViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AppsListUiEvent.NavigateHome -> onGoHomeRequest()
                is AppsListUiEvent.LaunchRelatedItem -> handleItemClick(event.item)
            }
        }
    }

    val scrollState = rememberLazyListState()

    // Swipe up from the top of the list to go back home, mirroring the system Back gesture.
    // The actual navigation is deferred to onPostFling (i.e. once the drag has actually ended)
    // rather than fired mid-drag from onPostScroll: calling animateScrollToPage() on the pager
    // while this list's own drag mutation is still active throws MutationInterruptedException,
    // since the pager is a nested-scroll ancestor participating in the same in-progress gesture.
    val swipeUpHomeConnection = remember(onGoHomeRequest) {
        object : NestedScrollConnection {
            var totalDrag = 0f
            var armed = false

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0) {
                    if (!armed) {
                        totalDrag += available.y

                        if (totalDrag < -120f) {
                            armed = true
                            return available
                        }
                    }
                } else {
                    totalDrag = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (armed) {
                    armed = false
                    onGoHomeRequest()
                }
                totalDrag = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // The main column with all the items in
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .nestedScroll(swipeUpHomeConnection),
            horizontalAlignment = appsListAlignment,
            verticalArrangement = Arrangement.Bottom
        ) {
            item {
                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(modifier = Modifier.height(statusBarHeight + 10.dp))
            }

            items(items, key = { item -> item.itemKey }) { item ->
                val screenTime = remember(appUsageList, item) {
                    if (item is LauncherItem.App) screenTimeViewModel.getScreenTime(item.app.packageName) else 0L
                }

                HomeScreenItem(
                    appName = item.displayName,
                    screenTime = formatScreenTime(screenTime),
                    onAppClick = { handleItemClick(item) },
                    onAppLongClick = { handleItemLongClick(item) },
                    showScreenTime = showScreenTimeApp && item is LauncherItem.App,
                    modifier = Modifier,
                    alignment = appsListAlignment,
                    shadow = showWallpaper,
                    color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary,
                    screenTimeColor = if (showWallpaper) MaterialTheme.colorScheme.secondaryFixed else MaterialTheme.colorScheme.primary,
                    screenTimeAlpha = if (showWallpaper) 1f else 0.5f
                )
            }

            item {
                Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
            }
        }
    }
}