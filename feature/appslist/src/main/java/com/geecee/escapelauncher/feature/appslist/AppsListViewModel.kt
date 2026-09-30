package com.geecee.escapelauncher.feature.appslist

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.domain.apps.AppActionType
import com.geecee.escapelauncher.core.domain.apps.GetAppActionsUseCase
import com.geecee.escapelauncher.core.domain.apps.GetAppShortcutsUseCase
import com.geecee.escapelauncher.core.domain.apps.OpenAppDetailsUseCase
import com.geecee.escapelauncher.core.domain.apps.RenameAppUseCase
import com.geecee.escapelauncher.core.domain.apps.RenameShortcutUseCase
import com.geecee.escapelauncher.core.domain.apps.StartShortcutUseCase
import com.geecee.escapelauncher.core.domain.apps.UninstallAppUseCase
import com.geecee.escapelauncher.core.domain.apps.UnpinShortcutUseCase
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.domain.search.SearchAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.*
import com.geecee.escapelauncher.core.model.AppAction
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.model.PinnedShortcut
import com.geecee.escapelauncher.core.model.RenameTarget
import com.geecee.escapelauncher.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppsListViewModel @Inject constructor(
    appearanceRepository: AppearanceRepository,
    searchSettingsRepository: SearchSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val favouritesRepository: FavouritesRepository,
    private val getAppActionsUseCase: GetAppActionsUseCase,
    private val getAppShortcutsUseCase: GetAppShortcutsUseCase,
    private val startShortcutUseCase: StartShortcutUseCase,
    private val unpinShortcutUseCase: UnpinShortcutUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val openAppDetailsUseCase: OpenAppDetailsUseCase,
    private val renameAppUseCase: RenameAppUseCase,
    private val renameShortcutUseCase: RenameShortcutUseCase,
    searchAppsUseCase: SearchAppsUseCase
) : ViewModel() {
    // UI Events
    private val _uiEvent = MutableSharedFlow<AppsListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Settings
    val showScreenTimeApp = screenTimeSettingsRepository.showScreenTimeApp
    val appsAlignment = appearanceRepository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }
    val showSearchBox = searchSettingsRepository.showSearchBox
    val searchAutoOpen = searchSettingsRepository.searchAutoOpen
    val automaticallyOpenAppsInSearch = searchSettingsRepository.automaticallyOpenAppsInSearch
    val hiddenAppsInSearch = searchSettingsRepository.showHiddenAppsInSearch
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled
    val showWallpaper = appearanceRepository.showWallpaper

    // Search
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded: StateFlow<Boolean> = _searchExpanded.asStateFlow()
    fun onSearchTextChanged(query: String) {
        if (_searchExpanded.value) {
            _searchText.value = query
        }
    }
    fun onSearchExpandedChanged(expanded: Boolean) {
        _searchExpanded.value = expanded
        if (!_searchExpanded.value) {
            _searchText.value = ""
        }
    }

    // Apps + pinned shortcuts, merged
    val items: StateFlow<List<LauncherItem>> = searchAppsUseCase(_searchText, hiddenAppsInSearch)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Starts a pinned shortcut clicked directly in the drawer (not via the bottom sheet). */
    fun openShortcut(shortcut: PinnedShortcut) {
        startShortcutUseCase(shortcut.packageName, shortcut.shortcutId)
        onSearchExpandedChanged(false)
        viewModelScope.launch {
            _uiEvent.emit(AppsListUiEvent.NavigateHome)
        }
    }

    // Bottom sheet
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }
    fun setBottomSheetApp(item: LauncherItem?) {
        _bottomSheetApp.value = item
    }
    private val _bottomSheetApp = MutableStateFlow<LauncherItem?>(null)
    val bottomSheetApp: StateFlow<LauncherItem?> = _bottomSheetApp.asStateFlow()

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
                                    _uiEvent.emit(AppsListUiEvent.NavigateHome)
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
                        _uiEvent.emit(AppsListUiEvent.NavigateHome)
                    }
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

sealed class AppsListUiEvent {
    data object NavigateHome : AppsListUiEvent()
}
