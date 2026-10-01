package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.relateditems.RelatedItemsRepository
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A UseCase that resolves the ordered list of "related items" (other apps/shortcuts) configured
 * for [ownerItemKey], for the quick-launch section shown in that item's long-press popup.
 */
class GetRelatedLauncherItemsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val pinnedShortcutsRepository: PinnedShortcutsRepository,
    private val relatedItemsRepository: RelatedItemsRepository
) {
    operator fun invoke(ownerItemKey: String): Flow<List<LauncherItem>> {
        return combine(
            appsRepository.mainUserApps,
            pinnedShortcutsRepository.getAllFlow(),
            relatedItemsRepository.getRelatedItemsFlow(ownerItemKey)
        ) { apps, shortcuts, order ->
            val itemsByKey = (apps.map { LauncherItem.App(it) } + shortcuts.map { LauncherItem.Shortcut(it) })
                .associateBy { it.itemKey }
            order.mapNotNull { entry -> itemsByKey[entry.relatedItemKey] }
        }
    }
}
