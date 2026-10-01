package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.RelatedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelatedItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: RelatedItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<RelatedItemEntity>)

    @Query("DELETE FROM relatedItems WHERE ownerItemKey = :ownerItemKey AND relatedItemKey = :relatedItemKey")
    suspend fun delete(ownerItemKey: String, relatedItemKey: String)

    // No Favourites analog: a shortcut can be removed while it's either an owner of a related
    // list or a target inside someone else's, so cleanup on unpin must check both directions.
    @Query("DELETE FROM relatedItems WHERE ownerItemKey = :itemKey OR relatedItemKey = :itemKey")
    suspend fun deleteAllReferencing(itemKey: String)

    @Query("SELECT * FROM relatedItems WHERE ownerItemKey = :ownerItemKey ORDER BY position ASC")
    fun getAllForOwnerFlow(ownerItemKey: String): Flow<List<RelatedItemEntity>>

    @Query("SELECT * FROM relatedItems WHERE ownerItemKey = :ownerItemKey ORDER BY position ASC")
    suspend fun getAllForOwner(ownerItemKey: String): List<RelatedItemEntity>
}
