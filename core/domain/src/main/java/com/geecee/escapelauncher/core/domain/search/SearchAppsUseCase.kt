package com.geecee.escapelauncher.core.domain.search

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import jakarta.inject.Inject

/***
 * Use case to return all installed apps and pinned shortcuts filtered with a query
 */
class SearchAppsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val pinnedShortcutsRepository: PinnedShortcutsRepository
) {
    operator fun invoke(queryFlow: Flow<String>, showHiddenFlow: Flow<Boolean>): Flow<List<LauncherItem>> {
        return combine(
            appsRepository.mainUserApps,
            pinnedShortcutsRepository.getAllFlow(),
            modifiedAppsRepository.getHiddenPackageIdsFlow(),
            queryFlow,
            showHiddenFlow
        ) { allApps, shortcuts, hiddenIds, rawQuery, showHidden ->
            val query = rawQuery.trim()
            val hiddenSet = hiddenIds.toSet()

            val items: List<LauncherItem> =
                allApps.map { LauncherItem.App(it) } + shortcuts.map { LauncherItem.Shortcut(it) }

            val filtered = if (query.isBlank()) {
                items
                    .filter { item -> item !is LauncherItem.App || !hiddenSet.contains(item.app.packageName) }
                    .sortedBy { it.displayName.lowercase() }
            } else {
                items.filter { item ->
                    val isHidden = item is LauncherItem.App && hiddenSet.contains(item.app.packageName)
                    val matchesQuery = fuzzyMatch(item.displayName, query)
                    matchesQuery && (!isHidden || showHidden)
                }
            }

            if (query.isNotBlank()) {
                sortItemsByRelevance(filtered, query)
            } else {
                filtered
            }
        }
    }
}
