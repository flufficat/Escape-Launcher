package com.geecee.escapelauncher.core.data.repository.relateditems

import com.geecee.escapelauncher.core.data.database.RelatedItemsDao
import com.geecee.escapelauncher.core.data.entity.RelatedItemEntity
import com.geecee.escapelauncher.core.domain.repository.relateditems.RelatedItemsRepository
import com.geecee.escapelauncher.core.model.RelatedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelatedItemsRepositoryImpl @Inject constructor(
    private val relatedItemsDao: RelatedItemsDao
) : RelatedItemsRepository {

    override fun getRelatedItemsFlow(ownerItemKey: String): Flow<List<RelatedItem>> =
        relatedItemsDao.getAllForOwnerFlow(ownerItemKey).map { entries -> entries.map { it.asExternalModel() } }

    override suspend fun addRelatedItem(ownerItemKey: String, relatedItemKey: String, relatedItemType: String) {
        val lastPos = relatedItemsDao.getAllForOwner(ownerItemKey).lastOrNull()?.position ?: -1.0
        relatedItemsDao.upsert(RelatedItemEntity(ownerItemKey, relatedItemKey, relatedItemType, lastPos + 1.0))
    }

    override suspend fun removeRelatedItem(ownerItemKey: String, relatedItemKey: String) {
        relatedItemsDao.delete(ownerItemKey, relatedItemKey)
    }

    override suspend fun reorderRelatedItem(
        ownerItemKey: String,
        relatedItemKey: String,
        relatedItemType: String,
        fromIndex: Int,
        toIndex: Int
    ) {
        val current = relatedItemsDao.getAllForOwner(ownerItemKey)
        if (fromIndex !in current.indices || toIndex !in current.indices) return // Ensure indices are within bounds to avoid IndexOutOfBoundsException if the list changed concurrently
        if (fromIndex == toIndex) return

        val others = current.filter { it.relatedItemKey != relatedItemKey }

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
                    tidyPositions(ownerItemKey)
                    val freshOthers = relatedItemsDao.getAllForOwner(ownerItemKey).filter { it.relatedItemKey != relatedItemKey }
                    val freshPrev = freshOthers[toIndex - 1].position
                    val freshNext = freshOthers[toIndex].position
                    (freshPrev + freshNext) / 2.0
                } else {
                    prevPos + (gap / 2.0)
                }
            }
        }

        relatedItemsDao.upsert(RelatedItemEntity(ownerItemKey, relatedItemKey, relatedItemType, newPosition))
    }

    override suspend fun removeAllReferencing(itemKey: String) {
        relatedItemsDao.deleteAllReferencing(itemKey)
    }

    private suspend fun tidyPositions(ownerItemKey: String) {
        val related = relatedItemsDao.getAllForOwner(ownerItemKey)
        val tidied = related.mapIndexed { index, entry ->
            entry.copy(position = index.toDouble())
        }
        relatedItemsDao.upsertAll(tidied)
    }
}

fun RelatedItemEntity.asExternalModel() = RelatedItem(
    ownerItemKey = ownerItemKey,
    relatedItemKey = relatedItemKey,
    relatedItemType = relatedItemType,
    position = position
)
