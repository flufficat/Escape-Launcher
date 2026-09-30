package com.geecee.escapelauncher.core.domain.repository.favourites

import com.geecee.escapelauncher.core.model.FavouriteOrder
import kotlinx.coroutines.flow.Flow

/**
 * Shared favourite ordering for apps and pinned shortcuts, keyed by [LauncherItem.itemKey][com.geecee.escapelauncher.core.model.LauncherItem.itemKey]
 * so both types can be favourited onto the home screen in one interleaved order.
 */
interface FavouritesRepository {
    fun getFavouriteOrderFlow(): Flow<List<FavouriteOrder>>
    suspend fun getFavouriteOrder(): List<FavouriteOrder>
    suspend fun isFavourite(itemKey: String): Boolean
    suspend fun getPosition(itemKey: String): Double?
    suspend fun addFavourite(itemKey: String, itemType: String)
    suspend fun removeFavourite(itemKey: String)

    /**
     * Reorders [itemKey] to [toIndex] (from [fromIndex]) within the favourite list.
     * When [scopeItemType] is non-null, [fromIndex]/[toIndex] are positions within the
     * subsequence of favourites whose type matches it (e.g. apps-only management screens);
     * when null, they're positions within the full interleaved list.
     */
    suspend fun reorderFavourite(
        itemKey: String,
        itemType: String,
        fromIndex: Int,
        toIndex: Int,
        scopeItemType: String? = null
    )

    suspend fun tidyFavouritePositions()
}
