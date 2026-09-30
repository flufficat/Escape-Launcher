package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.PinnedShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedShortcutsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PinnedShortcutEntity)

    @Query("DELETE FROM pinnedShortcuts WHERE packageName = :packageName AND shortcutId = :shortcutId")
    suspend fun delete(packageName: String, shortcutId: String)

    @Query("DELETE FROM pinnedShortcuts WHERE packageName = :packageName")
    suspend fun deleteAllForPackage(packageName: String)

    @Query("SELECT * FROM pinnedShortcuts")
    fun getAllFlow(): Flow<List<PinnedShortcutEntity>>

    @Query("SELECT * FROM pinnedShortcuts")
    suspend fun getAll(): List<PinnedShortcutEntity>

    @Query("SELECT * FROM pinnedShortcuts WHERE packageName = :packageName AND shortcutId = :shortcutId LIMIT 1")
    suspend fun get(packageName: String, shortcutId: String): PinnedShortcutEntity?

    @Query("SELECT * FROM pinnedShortcuts WHERE packageName = :packageName")
    suspend fun getAllForPackage(packageName: String): List<PinnedShortcutEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM pinnedShortcuts WHERE packageName = :packageName AND shortcutId = :shortcutId)")
    suspend fun isPinned(packageName: String, shortcutId: String): Boolean

    @Query("UPDATE pinnedShortcuts SET label = :label WHERE packageName = :packageName AND shortcutId = :shortcutId")
    suspend fun updateLabel(packageName: String, shortcutId: String, label: String)

    @Query("UPDATE pinnedShortcuts SET customLabel = :customLabel WHERE packageName = :packageName AND shortcutId = :shortcutId")
    suspend fun updateCustomLabel(packageName: String, shortcutId: String, customLabel: String?)
}
