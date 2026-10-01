package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.FavouriteOrderEntity
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import com.geecee.escapelauncher.core.data.entity.PinnedShortcutEntity
import com.geecee.escapelauncher.core.data.entity.RelatedItemEntity

@Database(
    entities = [
        ModifiedAppEntity::class,
        PinnedShortcutEntity::class,
        FavouriteOrderEntity::class,
        RelatedItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ModifiedAppsDatabase: RoomDatabase() {
    abstract fun modifiedAppsDao(): ModifiedAppsDao
    abstract fun pinnedShortcutsDao(): PinnedShortcutsDao
    abstract fun favouriteOrderDao(): FavouriteOrderDao
    abstract fun relatedItemsDao(): RelatedItemsDao
}
