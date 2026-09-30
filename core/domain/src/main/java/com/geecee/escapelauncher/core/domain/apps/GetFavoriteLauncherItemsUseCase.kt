package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A UseCase that combines favorited apps and pinned shortcuts into one interleaved,
 * ordered list of [LauncherItem]s for display on the home screen.
 */
class GetFavoriteLauncherItemsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val pinnedShortcutsRepository: PinnedShortcutsRepository,
    private val favouritesRepository: FavouritesRepository
) {
    operator fun invoke(): Flow<List<LauncherItem>> {
        return combine(
            appsRepository.mainUserApps,
            pinnedShortcutsRepository.getAllFlow(),
            favouritesRepository.getFavouriteOrderFlow()
        ) { apps, shortcuts, order ->
            val itemsByKey = (apps.map { LauncherItem.App(it) } + shortcuts.map { LauncherItem.Shortcut(it) })
                .associateBy { it.itemKey }
            order.mapNotNull { entry -> itemsByKey[entry.itemKey] }
        }
    }
}
