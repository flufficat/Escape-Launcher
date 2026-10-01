package com.geecee.escapelauncher.feature.homescreen

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.model.SearchGestureDirection
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.Clock
import com.geecee.escapelauncher.core.ui.composables.FirstTimeHelp
import com.geecee.escapelauncher.core.ui.composables.GlanceWidget
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.composables.RelatedItemsPickerDialog
import com.geecee.escapelauncher.core.ui.composables.RenameDialog
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.feature.newwidgets.WidgetRenderer
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import com.geecee.escapelauncher.feature.weather.WeatherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalResources
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    searchGestureDirection: SearchGestureDirection = SearchGestureDirection.UP,
    onSwipeDownOpenSearch: () -> Unit = {},
    onSwipeUpOpenSearch: () -> Unit = {},
    clockViewModel: ClockViewModel = hiltViewModel(),
    homeScreenViewModel: NewHomeScreenViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val timeParts by clockViewModel.timeParts.collectAsState()
    val twelveHourClock by homeScreenViewModel.twelveHourClock.collectAsState(initial = DefaultSettings.TWELVE_HOUR_CLOCK)
    val showClock by homeScreenViewModel.showClock.collectAsState(initial = DefaultSettings.SHOW_CLOCK)
    val bigClock by homeScreenViewModel.bigClock.collectAsState(initial = DefaultSettings.BIG_CLOCK)
    val showDate by homeScreenViewModel.showDate.collectAsState(initial = DefaultSettings.SHOW_DATE)
    val showScreenTimeHome by homeScreenViewModel.showScreenTimeHome.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_HOME)
    val showWeather by homeScreenViewModel.showWeather.collectAsState(initial = DefaultSettings.SHOW_WEATHER)
    val showScreenTimeApp by homeScreenViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val firstTimeHelp by homeScreenViewModel.firstTimeHelp.collectAsState(initial = false)
    val homeAlignment by homeScreenViewModel.homeAlignment.collectAsState(initial = DefaultSettingsUi.HOME_ALIGNMENT)
    val homeVAlignment by homeScreenViewModel.homeVAlignment.collectAsState(initial = DefaultSettingsUi.HOME_V_ALIGNMENT)
    val widgetOffsetPref by homeScreenViewModel.widgetOffset.collectAsState(initial = DefaultSettings.WIDGET_OFFSET)
    val hapticFeedbackEnabled by homeScreenViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val widgetHeight by homeScreenViewModel.widgetHeight.collectAsState(initial = DefaultSettings.WIDGET_HEIGHT)
    val widgetWidth by homeScreenViewModel.widgetWidth.collectAsState(initial = DefaultSettings.WIDGET_WIDTH)
    val widgetId by homeScreenViewModel.widgetId.collectAsState(initial = DefaultSettings.WIDGET_ID)
    val widgetHostManager = homeScreenViewModel.widgetHostManager
    val appUsageList by screenTimeViewModel.appUsageUiList.collectAsState()
    val favoriteItems by homeScreenViewModel.favoriteItems.collectAsState()
    val showBottomSheet by homeScreenViewModel.showBottomSheet.collectAsState()
    val bottomSheetApp by homeScreenViewModel.bottomSheetApp.collectAsState()
    val bottomSheetActions by homeScreenViewModel.bottomSheetActions.collectAsState()
    val shortcutActions by homeScreenViewModel.shortcutActions.collectAsState()
    val relatedItemActions by homeScreenViewModel.relatedItemActions.collectAsState()
    val renameDialogTarget by homeScreenViewModel.renameDialogTarget.collectAsState()
    val relatedItemsPickerTarget by homeScreenViewModel.relatedItemsPickerTarget.collectAsState()
    val allLauncherItems by homeScreenViewModel.allLauncherItems.collectAsState()
    val showWallpaper by homeScreenViewModel.showWallpaper.collectAsState(initial = false)

    val (hour, minute, _) = timeParts

    LaunchedEffect(twelveHourClock) {
        clockViewModel.startTicker(twelveHourClock)
    }

    val haptics = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val handleItemClick: (LauncherItem) -> Unit = { item ->
        when (item) {
            is LauncherItem.App -> onAppOpened(item.app)
            is LauncherItem.Shortcut -> homeScreenViewModel.openShortcut(item.shortcut)
        }
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    // Handle UI Events from ViewModel
    LaunchedEffect(Unit) {
        homeScreenViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is HomeUiEvent.NavigateHome -> onGoHomeRequest()
                is HomeUiEvent.LaunchRelatedItem -> handleItemClick(event.item)
            }
        }
    }

    val scrollState = rememberLazyListState()

    // Swipe down opens the notification shade by default, or search when chosen in Settings'
    // Gestures section; swipe up opens search only when chosen there (it does nothing here
    // otherwise - unlike the swipe-up gesture on the search page itself, which always goes home).
    // Both directions defer firing to onPostFling (once the drag has actually ended) rather than
    // mid-drag: starting a page-transition animation while this list's own drag mutation is still
    // active can get it interrupted by that ongoing gesture. Each direction only fires once per
    // gesture via its own armed flag.
    val nestedScrollConnection = remember(searchGestureDirection, onSwipeDownOpenSearch, onSwipeUpOpenSearch) {
        object : NestedScrollConnection {
            var downDrag = 0f
            var downArmed = false
            var upDrag = 0f
            var upArmed = false

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) {
                    downDrag = 0f
                    upDrag = 0f
                    return Offset.Zero
                }
                if (available.y > 0) {
                    upDrag = 0f
                    if (!downArmed) {
                        downDrag += available.y
                        if (downDrag > 150f) {
                            downArmed = true
                            return available
                        }
                    }
                } else if (available.y < 0) {
                    downDrag = 0f
                    if (!upArmed) {
                        upDrag += available.y
                        if (upDrag < -150f) {
                            upArmed = true
                            return available
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (downArmed) {
                    downArmed = false
                    if (searchGestureDirection == SearchGestureDirection.DOWN) {
                        onSwipeDownOpenSearch()
                    } else {
                        try {
                            @SuppressLint("WrongConstant") val service =
                                context.getSystemService("statusbar") // Use literal string "statusbar"

                            val statusBarManager = Class.forName("android.app.StatusBarManager")
                            val expand = statusBarManager.getMethod("expandNotificationsPanel")
                            expand.invoke(service)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (upArmed) {
                    upArmed = false
                    if (searchGestureDirection == SearchGestureDirection.UP) {
                        onSwipeUpOpenSearch()
                    }
                }
                downDrag = 0f
                upDrag = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            verticalArrangement = homeVAlignment,
            horizontalAlignment = homeAlignment,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp)
                .nestedScroll(nestedScrollConnection)
        ) {
            //Top padding
            item {
                Spacer(Modifier.height(90.dp))
            }

            //Clock
            item {
                if (showClock) {
                    Clock(
                        hour = hour,
                        minute = minute,
                        bigClock = bigClock,
                        homeAlignment = homeAlignment,
                        onClockClick = {
                            try {
                                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("Error", e.message.orEmpty())
                            }
                        },
                        color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary,
                        shadow = showWallpaper
                    )
                }
            }

            //Glance widgets
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (showDate) {
                        val dateFormat =
                            remember { SimpleDateFormat("EEE d MMM", Locale.getDefault()) }
                        var dateText by remember { mutableStateOf(dateFormat.format(Date())) }

                        LaunchedEffect(Unit) {
                            while (true) {
                                val calendar = Calendar.getInstance()
                                val now = calendar.timeInMillis
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                                calendar.set(Calendar.HOUR_OF_DAY, 0)
                                calendar.set(Calendar.MINUTE, 0)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
                                val delayMillis = calendar.timeInMillis - now
                                delay(delayMillis.milliseconds)
                                dateText = dateFormat.format(Date())
                            }
                        }

                        GlanceWidget(
                            text = dateText,
                            icon = null,
                            iconContentDescription = "",
                            homeAlignment = homeAlignment,
                            small = true,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    homeScreenViewModel.logException(e)
                                }
                            },
                            shadow = showWallpaper,
                            color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
                        )

                    }

                    if (showScreenTimeHome) {
                        val todayUsage by screenTimeViewModel.totalUsage.collectAsState()

                        GlanceWidget(
                            text = formatScreenTime(todayUsage),
                            icon = Icons.Default.Timer,
                            iconContentDescription = "Screen Time",
                            homeAlignment = homeAlignment,
                            small = true,
                            onClick = {},
                            shadow = showWallpaper,
                            color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
                        )

                    }

                    if (showWeather) {
                        @Suppress("KotlinConstantConditions", "RedundantSuppression") // This is to stop the IS_FOSS is always true cuz it's a FOSS sync in Android Studio
                        if (!homeScreenViewModel.isFoss) {
                            HomeWeatherImpl(
                                alignment = homeAlignment,
                                shadow = showWallpaper,
                                color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            //Widgets
            item {
                // Find out offset of widget
                val widgetOffset = remember(homeAlignment, widgetOffsetPref) {
                    val alignmentOffset = when (homeAlignment) {
                        Alignment.Start -> -8
                        Alignment.End -> 8
                        else -> 0
                    }
                    alignmentOffset + widgetOffsetPref.toInt()
                }

                WidgetRenderer(
                    appWidgetId = widgetId,
                    widgetHostManager = widgetHostManager,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (widgetOffset.dp).toPx().toInt(), 0
                            )
                        }
                        .size(
                            widgetWidth.dp,
                            widgetHeight.dp
                        )
                        .padding(0.dp, 0.dp))
            }

            //Apps + pinned shortcuts
            items(favoriteItems, key = { item -> item.itemKey }) { item ->
                val screenTime = remember(appUsageList, item) {
                    if (item is LauncherItem.App) screenTimeViewModel.getScreenTime(item.app.packageName) else 0L
                }

                HomeScreenItem(
                    appName = item.displayName,
                    screenTime = formatScreenTime(screenTime),
                    onAppClick = { handleItemClick(item) },
                    onAppLongClick = {
                        homeScreenViewModel.setBottomSheetVisible(true)
                        homeScreenViewModel.setBottomSheetApp(item)
                        doHapticFeedBack(hapticFeedback = haptics, enabled = hapticFeedbackEnabled)
                    },
                    showScreenTime = showScreenTimeApp && item is LauncherItem.App,
                    modifier = Modifier,
                    alignment = homeAlignment,
                    shadow = showWallpaper,
                    color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary,
                    screenTimeColor = if (showWallpaper) MaterialTheme.colorScheme.secondaryFixed else MaterialTheme.colorScheme.primary,
                    screenTimeAlpha = if (showWallpaper) 1f else 0.5f
                )
            }

            item {
                AnimatedVisibility(
                    visible = firstTimeHelp,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(Modifier.height(15.dp))

                        FirstTimeHelp(
                            swipeForAllAppsText = stringResource(R.string.swipe_for_all_apps),
                            holdForSettingsText = stringResource(R.string.hold_for_settings)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(90.dp))
            }
        }

        // Bottom Sheet
        if (showBottomSheet && bottomSheetApp != null) {
            HomeScreenBottomSheet(
                subject = bottomSheetApp!!,
                actions = bottomSheetActions,
                onDismissRequest = { homeScreenViewModel.setBottomSheetVisible(false) },
                shortcutActions = shortcutActions,
                relatedItemActions = relatedItemActions,
                sheetState = rememberBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
                )
            )
        }

        renameDialogTarget?.let { target ->
            RenameDialog(
                target = target,
                onDismiss = { homeScreenViewModel.dismissRenameDialog() },
                onSave = { homeScreenViewModel.saveRename(it) }
            )
        }

        relatedItemsPickerTarget?.let { owner ->
            val relatedItems by homeScreenViewModel.getRelatedLauncherItemsUseCase(owner.itemKey).collectAsState(initial = emptyList())
            RelatedItemsPickerDialog(
                owner = owner,
                items = allLauncherItems,
                selectedItems = relatedItems,
                title = stringResource(R.string.related_items_picker_title),
                onDismiss = { homeScreenViewModel.dismissRelatedItemsPicker() },
                onItemSelected = { item, selected ->
                    coroutineScope.launch {
                        if (selected) {
                            homeScreenViewModel.relatedItemsRepository.removeRelatedItem(owner.itemKey, item.itemKey)
                        } else {
                            homeScreenViewModel.relatedItemsRepository.addRelatedItem(owner.itemKey, item.itemKey, item.itemType)
                        }
                    }
                },
                onItemMoved = { fromIndex, toIndex ->
                    val item = relatedItems[fromIndex]
                    coroutineScope.launch {
                        homeScreenViewModel.relatedItemsRepository.reorderRelatedItem(
                            ownerItemKey = owner.itemKey,
                            relatedItemKey = item.itemKey,
                            relatedItemType = item.itemType,
                            fromIndex = fromIndex,
                            toIndex = toIndex
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun HomeWeatherImpl(
    alignment: Alignment.Horizontal,
    color: Color = MaterialTheme.colorScheme.primary,
    shadow: Boolean = false,
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val weatherAppPackage by weatherViewModel.weatherAppPackage.collectAsState(initial = DefaultSettings.WEATHER_APP_PACKAGE)

    AnimatedVisibility(
        weatherViewModel.weatherText.value != "",
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        GlanceWidget(
            text = weatherViewModel.weatherText.value,
            icon = Icons.Default.WbSunny,
            iconContentDescription = "Weather",
            shadow = shadow,
            homeAlignment = alignment,
            small = true,
            color = color,
            onClick = {
                if (weatherAppPackage.isNotEmpty()) {
                    //todo: use OpenApp() here so it's tracked

                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(
                            weatherAppPackage
                        )
                    launchIntent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.set_weather_app_in_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}
