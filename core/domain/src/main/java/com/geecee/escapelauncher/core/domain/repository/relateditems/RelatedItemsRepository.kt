package com.geecee.escapelauncher.core.domain.repository.relateditems

import com.geecee.escapelauncher.core.model.RelatedItem
import kotlinx.coroutines.flow.Flow

/**
 * Per-owner, ordered lists of other apps/shortcuts shown as quick-launch entries in a given
 * app or shortcut's long-press popup. Each owner's list is independent of every other's -
 * cycles (A relates to B, B relates to A) are allowed and harmless.
 */
interface RelatedItemsRepository {
    fun getRelatedItemsFlow(ownerItemKey: String): Flow<List<RelatedItem>>
    suspend fun addRelatedItem(ownerItemKey: String, relatedItemKey: String, relatedItemType: String)
    suspend fun removeRelatedItem(ownerItemKey: String, relatedItemKey: String)
    suspend fun reorderRelatedItem(
        ownerItemKey: String,
        relatedItemKey: String,
        relatedItemType: String,
        fromIndex: Int,
        toIndex: Int
    )

    /** Cleans up [itemKey]'s own related list and any entry of it inside someone else's. */
    suspend fun removeAllReferencing(itemKey: String)
}
