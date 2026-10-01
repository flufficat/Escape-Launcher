package com.geecee.escapelauncher.feature.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.domain.apps.*
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.domain.repository.relateditems.RelatedItemsRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.*
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.model.PinnedShortcut
import com.geecee.escapelauncher.core.model.RenameTarget
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.map
import com.geecee.escapelauncher.core.domain.apps.UninstallAppUseCase
import com.geecee.escapelauncher.core.domain.apps.OpenAppDetailsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    appearanceRepository: AppearanceRepository,
    clockRepository: ClockRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    onboardingRepository: OnboardingRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    weatherSettingsRepository: WeatherSettingsRepository,
    widgetSettingsRepository: WidgetSettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val favouritesRepository: FavouritesRepository,
    val relatedItemsRepository: RelatedItemsRepository,
    appsRepository: AppsRepository,
    pinnedShortcutsRepository: PinnedShortcutsRepository,
    getFavoriteLauncherItemsUseCase: GetFavoriteLauncherItemsUseCase,
    val widgetHostManager: WidgetHostManager,
    appConfiguration: AppConfiguration,
    private val getAppActionsUseCase: GetAppActionsUseCase,
    private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
    val getRelatedLauncherItemsUseCase: GetRelatedLauncherItemsUseCase,
    private val startShortcutUseCase: StartShortcutUseCase,
    private val unpinShortcutUseCase: UnpinShortcutUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val openAppDetailsUseCase: OpenAppDetailsUseCase,
    private val renameAppUseCase: RenameAppUseCase,
    private val renameShortcutUseCase: RenameShortcutUseCase,
    private val analyticsProxy: AnalyticsProxy
) : ViewModel() {
    val isFoss = appConfiguration.isFoss

    // UI Events
    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Settings
    val twelveHourClock = clockRepository.twelveHourClock
    val showClock = clockRepository.showClock
    val bigClock = clockRepository.bigClock
    val showDate = clockRepository.showDate
    val showScreenTimeHome = screenTimeSettingsRepository.showScreenTimeHome
    val showWeather = weatherSettingsRepository.showWeather
    val showScreenTimeApp = screenTimeSettingsRepository.showScreenTimeApp
    val firstTimeHelp = onboardingRepository.firstTimeHelp
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled
    val showWallpaper = appearanceRepository.showWallpaper

    val homeAlignment = appearanceRepository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val homeVAlignment = appearanceRepository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            else -> Arrangement.Bottom
        }
    }

    val widgetOffset = widgetSettingsRepository.widgetOffset
    val widgetHeight = widgetSettingsRepository.widgetHeight
    val widgetWidth = widgetSettingsRepository.widgetWidth
    val widgetId = widgetSettingsRepository.widgetId

    // Favorite apps + pinned shortcuts, interleaved in one shared order
    val favoriteItems: StateFlow<List<LauncherItem>> = getFavoriteLauncherItemsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Every app and pinned shortcut, unfiltered - candidates for the related-items picker. */
    val allLauncherItems: StateFlow<List<LauncherItem>> = combine(
        appsRepository.mainUserApps,
        pinnedShortcutsRepository.getAllFlow()
    ) { apps, shortcuts ->
        apps.map { LauncherItem.App(it) } + shortcuts.map { LauncherItem.Shortcut(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Starts a favourited pinned shortcut tapped directly on the home screen. */
    fun openShortcut(shortcut: PinnedShortcut) {
        startShortcutUseCase(shortcut.packageName, shortcut.shortcutId)
    }

    // Bottom Sheet State
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }

    private val _bottomSheetApp = MutableStateFlow<LauncherItem?>(null)
    val bottomSheetApp: StateFlow<LauncherItem?> = _bottomSheetApp.asStateFlow()
    fun setBottomSheetApp(item: LauncherItem?) {
        _bottomSheetApp.value = item
    }

    // Rename dialog
    private val _renameDialogTarget = MutableStateFlow<RenameTarget?>(null)
    val renameDialogTarget: StateFlow<RenameTarget?> = _renameDialogTarget.asStateFlow()
    fun dismissRenameDialog() {
        _renameDialogTarget.value = null
    }
    fun saveRename(newName: String) {
        val target = _renameDialogTarget.value ?: return
        viewModelScope.launch {
            when (target) {
                is RenameTarget.App -> renameAppUseCase(target.packageId, newName)
                is RenameTarget.Shortcut -> renameShortcutUseCase(target.packageName, target.shortcutId, newName)
            }
            _renameDialogTarget.value = null
        }
    }

    // Related items picker
    private val _relatedItemsPickerTarget = MutableStateFlow<LauncherItem?>(null)
    val relatedItemsPickerTarget: StateFlow<LauncherItem?> = _relatedItemsPickerTarget.asStateFlow()
    fun dismissRelatedItemsPicker() {
        _relatedItemsPickerTarget.value = null
    }

    fun logException(e: Exception) {
        analyticsProxy.recordException(e)
    }

    // Actions
    val bottomSheetActions: StateFlow<List<AppAction>> = _bottomSheetApp.flatMapLatest { item ->
        if (item == null) flowOf(emptyList())
        else getAppActionsUseCase(item).map { actionTypes ->
            actionTypes.map { type ->
                when (type) {
                    AppActionType.Uninstall -> AppAction(
                        labelRes = R.string.uninstall,
                        onClick = { clicked ->
                            if (clicked is LauncherItem.App) {
                                uninstallAppUseCase(clicked.app)
                            }
                            _showBottomSheet.value = false
                        }
                    )
                    is AppActionType.ToggleFavorite -> AppAction(
                        labelRes = if (type.isFavorite) R.string.rem_from_fav else R.string.add_to_fav,
                        isVisible = { clicked -> clicked !is LauncherItem.App || clicked.app.isMainUserApp() },
                        onClick = { clicked ->
                            viewModelScope.launch {
                                if (type.isFavorite) {
                                    favouritesRepository.removeFavourite(clicked.itemKey)
                                } else {
                                    favouritesRepository.addFavourite(clicked.itemKey, clicked.itemType)
                                    _uiEvent.emit(HomeUiEvent.NavigateHome)
                                }
                                _showBottomSheet.value = false
                            }
                        }
                    )
                    AppActionType.Hide -> AppAction(
                        labelRes = R.string.hide,
                        isVisible = { clicked -> clicked is LauncherItem.App && clicked.app.isMainUserApp() },
                        onClick = { clicked ->
                            if (clicked is LauncherItem.App) {
                                viewModelScope.launch {
                                    modifiedAppsRepository.setHidden(clicked.app.packageName, true)
                                    _showBottomSheet.value = false
                                }
                            }
                        }
                    )
                    AppActionType.AppInfo -> AppAction(
                        labelRes = R.string.app_info,
                        isVisible = { clicked -> clicked is LauncherItem.App },
                        onClick = { clicked ->
                            if (clicked is LauncherItem.App) {
                                openAppDetailsUseCase(clicked.app)
                            }
                            _showBottomSheet.value = false
                        }
                    )
                    AppActionType.AddChallenge -> AppAction(
                        labelRes = R.string.add_open_challenge,
                        isVisible = { clicked -> clicked is LauncherItem.App && clicked.app.isMainUserApp() },
                        onClick = { clicked ->
                            if (clicked is LauncherItem.App) {
                                viewModelScope.launch {
                                    modifiedAppsRepository.setChallenge(clicked.app.packageName, true)
                                    _showBottomSheet.value = false
                                }
                            }
                        }
                    )
                    AppActionType.Rename -> AppAction(
                        labelRes = R.string.rename,
                        onClick = { clicked ->
                            _renameDialogTarget.value = when (clicked) {
                                is LauncherItem.App -> RenameTarget.App(clicked.app.packageName, clicked.displayName)
                                is LauncherItem.Shortcut -> RenameTarget.Shortcut(
                                    clicked.shortcut.packageName,
                                    clicked.shortcut.shortcutId,
                                    clicked.displayName
                                )
                            }
                            _showBottomSheet.value = false
                        }
                    )
                    AppActionType.RemoveShortcut -> AppAction(
                        labelRes = R.string.remove,
                        isVisible = { clicked -> clicked is LauncherItem.Shortcut },
                        onClick = { clicked ->
                            if (clicked is LauncherItem.Shortcut) {
                                viewModelScope.launch {
                                    unpinShortcutUseCase(clicked.shortcut.packageName, clicked.shortcut.shortcutId)
                                    _showBottomSheet.value = false
                                }
                            }
                        }
                    )
                    AppActionType.ManageRelatedItems -> AppAction(
                        labelRes = R.string.add_related,
                        isVisible = { clicked -> clicked !is LauncherItem.App || clicked.app.isMainUserApp() },
                        onClick = { clicked ->
                            _relatedItemsPickerTarget.value = clicked
                            _showBottomSheet.value = false
                        }
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shortcutActions: StateFlow<List<AppAction>> = _bottomSheetApp.map { item ->
        if (item !is LauncherItem.App || !item.app.isMainUserApp()) return@map emptyList()

        getAppShortcutsUseCase(item.app.packageName).map { shortcut ->
            AppAction(
                label = shortcut.label,
                onClick = { clicked ->
                    if (clicked is LauncherItem.App) {
                        startShortcutUseCase(clicked.app.packageName, shortcut.id)
                    }
                    _showBottomSheet.value = false
                    viewModelScope.launch {
                        _uiEvent.emit(HomeUiEvent.NavigateHome)
                    }
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val relatedItemActions: StateFlow<List<AppAction>> = _bottomSheetApp.flatMapLatest { item ->
        if (item == null) flowOf(emptyList())
        else getRelatedLauncherItemsUseCase(item.itemKey).map { relatedItems ->
            relatedItems.map { related ->
                AppAction(
                    label = related.displayName,
                    onClick = {
                        _showBottomSheet.value = false
                        viewModelScope.launch {
                            _uiEvent.emit(HomeUiEvent.LaunchRelatedItem(related))
                        }
                    }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

sealed class HomeUiEvent {
    data object NavigateHome : HomeUiEvent()
    data class LaunchRelatedItem(val item: LauncherItem) : HomeUiEvent()
}
