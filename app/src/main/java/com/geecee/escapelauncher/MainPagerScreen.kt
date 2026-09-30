package com.geecee.escapelauncher

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.model.SearchGestureDirection
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.core.ui.composables.TabDisplay
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.appslist.AppsList
import com.geecee.escapelauncher.feature.appslist.AppsListViewModel
import com.geecee.escapelauncher.feature.homescreen.HomeScreen
import com.geecee.escapelauncher.feature.screentime.ScreenTimeDashboard
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.securefolder.SecureFolderButton
import com.geecee.escapelauncher.feature.securefolder.canUseSecureFolder
import com.geecee.escapelauncher.feature.workapps.WorkApps
import com.geecee.escapelauncher.privatespace.PrivateSpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 *  Main composable for home screen:
 *  contains a pager with all the pages inside of it, contains bottom sheet, contains open challenge UI
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)
@Composable
fun MainPagerScreen(
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    onOpenSettings: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val hideScreenTimePage by viewModel.hideScreenTimePage.collectAsState()
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = DefaultSettings.DOUBLE_TAP_TO_LOCK)
    val hapticFeedbackEnabled by viewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val screenTimePageIndex = if (!hideScreenTimePage) 0 else -1
    val homePageIndex = if (hideScreenTimePage) 0 else 1
    val appsListPageIndex = if (hideScreenTimePage) 1 else 2

    val isAppsListVisible = viewModel.pagerState.currentPage == appsListPageIndex
    val searchText by appsListViewModel.searchText.collectAsState()
    val searchExpanded by appsListViewModel.searchExpanded.collectAsState()

    // Back should collapse an active search before it navigates home, so the two are always two
    // distinct, deterministic presses rather than relying on the OS's own IME-dismiss timing to
    // have already consumed a "first" press (see: this used to intermittently just clear the
    // search text on what should have been the press that goes home).
    BackHandler(enabled = true) {
        coroutineScope.launch {
            if (isAppsListVisible && (searchExpanded || searchText.isNotEmpty())) {
                appsListViewModel.onSearchExpandedChanged(false)
            } else {
                viewModel.animatedGoToMainPage()
            }
        }
    }

    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    val appsListTabs = listOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canUseSecureFolder(context = context)) {
            TabbedScreen(
                title = "Secure Folder", icon = Icons.Default.Lock, content = {
                    SecureFolderButton(
                        modifier = Modifier.fillMaxSize()
                    )
                })
        } else {
            null
        },
        if (isDefaultLauncher && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && viewModel.managedProfileExists(
                ManagedProfileType.PrivateSpace
            )
        ) {
            TabbedScreen(
                title = "Private", icon = Icons.Default.Lock, content = {
                    PrivateSpace(
                        modifier = Modifier
                            .fillMaxSize(),
                        onAppClick = { app ->
                            viewModel.openApp(
                                app = app, overrideChallenge = false, onAppOpened = {
                                    screenTimeViewModel.onAppOpened(it)
                                    appsListViewModel.onSearchExpandedChanged(false)
                                    doHapticFeedBack(haptics, hapticFeedbackEnabled)
                                })
                        },
                        onAppLongClick = { app ->
                            appsListViewModel.setBottomSheetVisible(true)
                            appsListViewModel.setBottomSheetApp(LauncherItem.App(app))
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        })

                })
        } else {
            null
        },
        if (viewModel.isManagedProfileSupported(type = ManagedProfileType.WorkApps) && viewModel.managedProfileExists(
                type = ManagedProfileType.WorkApps
            )
        ) {
            TabbedScreen(
                title = "Work", icon = Icons.Default.Work, content = {
                    WorkApps(modifier =
                        Modifier.fillMaxSize(),
                        onAppClick = { app ->
                        viewModel.openApp(
                            app = app, overrideChallenge = false, onAppOpened = {
                                screenTimeViewModel.onAppOpened(it)
                                appsListViewModel.onSearchExpandedChanged(false)
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            })
                    },
                        onAppLongClick = { app ->
                        appsListViewModel.setBottomSheetVisible(true)
                        appsListViewModel.setBottomSheetApp(LauncherItem.App(app))
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    })
                })
        } else {
            null
        }
    )

    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = DefaultSettings.SEARCH_AUTO_OPEN)
    val searchGestureDirection by viewModel.searchGestureDirection.collectAsState()

    // Tidy up apps list when it closes or opens
    LaunchedEffect(isAppsListVisible) {
        if (!isAppsListVisible) {
            appsListViewModel.onSearchExpandedChanged(false)
            appsListViewModel.setBottomSheetVisible(visibility = false)
        } else if (autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

    // The horizontal swipe from home to the apps list/search page is the default ("Left") gesture,
    // but Settings' Gestures section lets the user reassign search-opening to Down or Up instead -
    // in which case this direction should do nothing on the home page. Block just that one
    // directional drag/fling (home -> apps list) unless Left is the chosen direction; the
    // home <-> screen-time direction is left untouched regardless of this setting.
    val pagerLeftGestureConnection = remember(searchGestureDirection, homePageIndex, appsListPageIndex) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (searchGestureDirection != SearchGestureDirection.LEFT &&
                    source == NestedScrollSource.UserInput &&
                    viewModel.pagerState.currentPage == homePageIndex &&
                    available.x < 0
                ) {
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (searchGestureDirection != SearchGestureDirection.LEFT &&
                    viewModel.pagerState.currentPage == homePageIndex &&
                    available.x < 0
                ) {
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    // Home Screen Pages
    HorizontalPager(
        state = viewModel.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pagerLeftGestureConnection)
            .combinedClickable(
                onClick = {},
                onLongClickLabel = "",
                onLongClick = {
                    onOpenSettings()
                    viewModel.setFirstTimeHelp(false)
                },
                indication = null,
                interactionSource = viewModel.interactionSource,
                onDoubleClick = {
                    // Turn screen off
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (doubleTapToLock) {
                            viewModel.lockScreen()
                        }
                    }
                })
    ) { page ->
        when (page) {
            screenTimePageIndex -> ScreenTimeDashboard(onGoHomeRequest = { globalViewModel.requestToGoHome() })

            homePageIndex -> HomeScreen(onAppOpened = { app ->
                viewModel.openApp(
                    app = app, overrideChallenge = false, onAppOpened = {
                        screenTimeViewModel.onAppOpened(it)
                    })
            },
                onGoHomeRequest = { globalViewModel.requestToGoHome() },
                searchGestureDirection = searchGestureDirection,
                onSwipeDownOpenSearch = {
                    // Expand the search box (and its keyboard) immediately, in step with the page
                    // transition starting, rather than waiting for the reactive isAppsListVisible
                    // effect further down to catch up once the page has already mostly arrived -
                    // that reactive path is a beat behind and makes the keyboard feel like a
                    // separate, delayed second step instead of one fluid motion.
                    if (autoOpenSearch) appsListViewModel.onSearchExpandedChanged(true)
                    coroutineScope.launch { viewModel.openSearchPageFromGesture(SearchGestureDirection.DOWN) }
                },
                onSwipeUpOpenSearch = {
                    if (autoOpenSearch) appsListViewModel.onSearchExpandedChanged(true)
                    coroutineScope.launch { viewModel.openSearchPageFromGesture(SearchGestureDirection.UP) }
                })

            appsListPageIndex -> {
                val showSearchBox by appsListViewModel.showSearchBox.collectAsState(initial = DefaultSettings.SHOW_SEARCH_BOX)
                val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
                val selectedTabIndex = remember { mutableIntStateOf(0) }
                val items by appsListViewModel.items.collectAsState()
                val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsState(initial = DefaultSettings.AUTOMATICALLY_OPEN_APPS_IN_SEARCH)

                val showBottomSheet by appsListViewModel.showBottomSheet.collectAsState()
                val bottomSheetApp by appsListViewModel.bottomSheetApp.collectAsState()
                val bottomSheetActions by appsListViewModel.bottomSheetActions.collectAsState()
                val shortcutActions by appsListViewModel.shortcutActions.collectAsState()

                val handleAppClick: (InstalledApp) -> Unit = { app ->
                    viewModel.openApp(
                        app = app, overrideChallenge = false, onAppOpened = {
                            screenTimeViewModel.onAppOpened(it)
                            appsListViewModel.onSearchExpandedChanged(false)
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        })
                }

                val handleItemClick: (LauncherItem) -> Unit = { item ->
                    when (item) {
                        is LauncherItem.App -> handleAppClick(item.app)
                        is LauncherItem.Shortcut -> appsListViewModel.openShortcut(item.shortcut)
                    }
                }

                // When search was reached via the Up/Down home-screen gesture, the pager itself
                // jumped here instantly (it only animates horizontally, which would look wrong for
                // a vertical gesture) - this page's content plays its own vertical slide-in from
                // that direction instead, so it still visually enters from the edge that was
                // swiped. Reached the normal way (a horizontal pager drag, i.e. Left), this stays
                // at zero offset and the pager's own drag handles the transition as before.
                val searchEntryDirection by viewModel.searchEntryDirection.collectAsState()
                val verticalOffsetPx = remember { Animatable(0f) }

                LaunchedEffect(searchEntryDirection) {
                    when (searchEntryDirection) {
                        SearchGestureDirection.UP -> {
                            verticalOffsetPx.snapTo(3000f)
                            verticalOffsetPx.animateTo(0f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
                            viewModel.consumeSearchEntryDirection()
                        }
                        SearchGestureDirection.DOWN -> {
                            verticalOffsetPx.snapTo(-3000f)
                            verticalOffsetPx.animateTo(0f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
                            viewModel.consumeSearchEntryDirection()
                        }
                        SearchGestureDirection.LEFT, null -> Unit
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, verticalOffsetPx.value.roundToInt()) }
                ) {
                    TabDisplay(
                        screens = listOf(
                            TabbedScreen(
                                title = "All Apps",
                                icon = Icons.Rounded.Apps,
                                content = { padding ->
                                    AppsList(
                                        padding = padding,
                                        onAppOpened = { app ->
                                            handleAppClick(app)
                                        },
                                        onGoHomeRequest = {
                                            globalViewModel.requestToGoHome()
                                        },
                                        appsListViewModel = appsListViewModel,
                                        screenTimeViewModel = screenTimeViewModel
                                    )
                                }
                            )
                        ) + appsListTabs.filterNotNull(),
                        selectedTabIndex = selectedTabIndex,
                        alignment = appsListAlignment,
                        showSearch = showSearchBox,
                        searchText = searchText,
                        searchExpanded = searchExpanded,
                        onSearchExpandedChange = {
                            appsListViewModel.onSearchExpandedChanged(it)
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        },
                        onSearchTextChanged = { query: String ->
                            appsListViewModel.onSearchTextChanged(query)
                            if (autoOpenAppInSearch && query.length >= 2 && items.size == 1) {
                                handleItemClick(items.first())
                            }
                        },
                        onSearchDone = { _: String, keyboardController: SoftwareKeyboardController? ->
                            if (items.isNotEmpty()) {
                                keyboardController?.hide()
                                handleItemClick(items.first())
                            } else {
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            }
                        }
                    )

                    // Bottom Sheet
                    AnimatedVisibility(showBottomSheet && bottomSheetApp != null) {
                        HomeScreenBottomSheet(
                            subject = bottomSheetApp!!,
                            actions = bottomSheetActions,
                            onDismissRequest = { appsListViewModel.setBottomSheetVisible(false) },
                            shortcutActions = shortcutActions,
                            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
                        )
                    }
                }
            }
        }
    }

    //Open Challenge
    AnimatedVisibility(
        visible = viewModel.showOpenChallenge.value, enter = fadeIn(), exit = fadeOut()
    ) {
        OpenChallenge(
            haptics = LocalHapticFeedback.current,
            enabled = hapticFeedbackEnabled,
            openApp = {
                viewModel.openApp(
                    app = viewModel.currentSelectedApp.value,
                    overrideChallenge = true,
                    onAppOpened = {
                        screenTimeViewModel.onAppOpened(it)
                    })
                coroutineScope.launch {
                    delay(1000.milliseconds)
                    viewModel.showOpenChallenge.value = false
                }
            },
            goBack = {
                viewModel.showOpenChallenge.value = false
            })
    }
}
