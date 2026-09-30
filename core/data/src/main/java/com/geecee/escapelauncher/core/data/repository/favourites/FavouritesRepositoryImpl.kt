package com.geecee.escapelauncher.core.data.repository.favourites

import com.geecee.escapelauncher.core.data.database.FavouriteOrderDao
import com.geecee.escapelauncher.core.data.entity.FavouriteOrderEntity
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.model.FavouriteOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepositoryImpl @Inject constructor(
    private val favouriteOrderDao: FavouriteOrderDao
) : FavouritesRepository {

    override fun getFavouriteOrderFlow(): Flow<List<FavouriteOrder>> =
        favouriteOrderDao.getAllFlow().map { entries -> entries.map { it.asExternalModel() } }

    override suspend fun getFavouriteOrder(): List<FavouriteOrder> =
        favouriteOrderDao.getAll().map { it.asExternalModel() }

    override suspend fun isFavourite(itemKey: String): Boolean =
        favouriteOrderDao.isFavourite(itemKey)

    override suspend fun getPosition(itemKey: String): Double? =
        favouriteOrderDao.getPosition(itemKey)

    override suspend fun addFavourite(itemKey: String, itemType: String) {
        val lastPos = favouriteOrderDao.getAll().lastOrNull()?.position ?: -1.0
        favouriteOrderDao.upsert(FavouriteOrderEntity(itemKey, itemType, lastPos + 1.0))
    }

    override suspend fun removeFavourite(itemKey: String) {
        favouriteOrderDao.delete(itemKey)
    }

    override suspend fun reorderFavourite(
        itemKey: String,
        itemType: String,
        fromIndex: Int,
        toIndex: Int,
        scopeItemType: String?
    ) {
        val scoped = scopedFavourites(scopeItemType)
        if (fromIndex !in scoped.indices || toIndex !in scoped.indices) return // Ensure indices are within bounds to avoid IndexOutOfBoundsException if the list changed concurrently
        if (fromIndex == toIndex) return

        val others = scoped.filter { it.itemKey != itemKey }

        val newPosition: Double = when {
            toIndex == 0 -> {
                (others.firstOrNull()?.position ?: 0.0) - 1.0
            }
            toIndex >= others.size -> {
                (others.lastOrNull()?.position ?: 0.0) + 1.0
            }
            else -> {
                val prevPos = others[toIndex - 1].position
                val nextPos = others[toIndex].position

                val gap = nextPos - prevPos
                if (gap < 1e-10) {
                    // If the gap is too small, we should tidy first and then recalculate
                    tidyFavouritePositions()
                    val freshOthers = scopedFavourites(scopeItemType).filter { it.itemKey != itemKey }
                    val freshPrev = freshOthers[toIndex - 1].position
                    val freshNext = freshOthers[toIndex].position
                    (freshPrev + freshNext) / 2.0
                } else {
                    prevPos + (gap / 2.0)
                }
            }
        }

        favouriteOrderDao.upsert(FavouriteOrderEntity(itemKey, itemType, newPosition))
    }

    override suspend fun tidyFavouritePositions() {
        val favourites = favouriteOrderDao.getAll()
        val tidied = favourites.mapIndexed { index, entry ->
            entry.copy(position = index.toDouble())
        }
        favouriteOrderDao.upsertAll(tidied)
    }

    private suspend fun scopedFavourites(scopeItemType: String?): List<FavouriteOrderEntity> {
        val all = favouriteOrderDao.getAll()
        return if (scopeItemType != null) all.filter { it.itemType == scopeItemType } else all
    }
}

fun FavouriteOrderEntity.asExternalModel() = FavouriteOrder(
    itemKey = itemKey,
    itemType = itemType,
    position = position
)
