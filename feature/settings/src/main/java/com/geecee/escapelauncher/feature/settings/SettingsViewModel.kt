package com.geecee.escapelauncher.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.apps.GetFavoriteLauncherItemsUseCase
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    appsRepository: AppsRepository,
    pinnedShortcutsRepository: PinnedShortcutsRepository,
    val modifiedAppsRepository: ModifiedAppsRepository,
    val favouritesRepository: FavouritesRepository,
    getFavoriteLauncherItemsUseCase: GetFavoriteLauncherItemsUseCase
) : ViewModel() {
    val installedApps = appsRepository.mainUserApps

    /** Every app and pinned shortcut that can be favourited, for the "manage favourites" screen. */
    val favouritableItems: StateFlow<List<LauncherItem>> = combine(
        appsRepository.mainUserApps,
        pinnedShortcutsRepository.getAllFlow()
    ) { apps, shortcuts ->
        (apps.map { LauncherItem.App(it) } + shortcuts.map { LauncherItem.Shortcut(it) })
            .sortedBy { it.displayName.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteItems: StateFlow<List<LauncherItem>> = getFavoriteLauncherItemsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
