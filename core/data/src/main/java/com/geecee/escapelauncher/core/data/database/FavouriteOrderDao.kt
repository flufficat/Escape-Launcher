package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.FavouriteOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FavouriteOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<FavouriteOrderEntity>)

    @Query("DELETE FROM favouriteOrder WHERE itemKey = :itemKey")
    suspend fun delete(itemKey: String)

    @Query("SELECT * FROM favouriteOrder ORDER BY position ASC")
    fun getAllFlow(): Flow<List<FavouriteOrderEntity>>

    @Query("SELECT * FROM favouriteOrder ORDER BY position ASC")
    suspend fun getAll(): List<FavouriteOrderEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favouriteOrder WHERE itemKey = :itemKey)")
    suspend fun isFavourite(itemKey: String): Boolean

    @Query("SELECT position FROM favouriteOrder WHERE itemKey = :itemKey LIMIT 1")
    suspend fun getPosition(itemKey: String): Double?
}
