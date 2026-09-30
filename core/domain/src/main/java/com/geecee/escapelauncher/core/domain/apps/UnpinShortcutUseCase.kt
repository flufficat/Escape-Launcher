package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import jakarta.inject.Inject

/**
 * Removes a pinned shortcut: rebuilds the remaining OS-level pinned set for the owning app
 * (required since [android.content.pm.LauncherApps.pinShortcuts] replaces the whole set, not
 * just one entry), then drops the local record and any favourite ordering for it.
 */
class UnpinShortcutUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val pinnedShortcutsRepository: PinnedShortcutsRepository,
    private val favouritesRepository: FavouritesRepository
) {
    suspend operator fun invoke(packageName: String, shortcutId: String) {
        val remainingIds = pinnedShortcutsRepository.getAllForPackage(packageName)
            .filter { it.shortcutId != shortcutId }
            .map { it.shortcutId }

        appsRepository.setPinnedShortcuts(packageName, remainingIds)
        pinnedShortcutsRepository.delete(packageName, shortcutId)
        favouritesRepository.removeFavourite("${LauncherItem.TYPE_SHORTCUT}:$packageName:$shortcutId")
    }
}
