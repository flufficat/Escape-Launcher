package com.geecee.escapelauncher.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds pinned-shortcut storage and a shared apps+shortcuts favourite-ordering table. Existing
 * app favourite order (modifiedApps.favouritePosition) is copied into the new shared table so
 * current users keep their order; that column is left in place afterward rather than dropped
 * (which would need a costlier table rebuild) - app code just stops reading/writing it.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pinnedShortcuts` (
                `packageName` TEXT NOT NULL,
                `shortcutId` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `customLabel` TEXT,
                `pinnedAt` INTEGER NOT NULL,
                PRIMARY KEY(`packageName`, `shortcutId`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `favouriteOrder` (
                `itemKey` TEXT NOT NULL,
                `itemType` TEXT NOT NULL,
                `position` REAL NOT NULL,
                PRIMARY KEY(`itemKey`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO favouriteOrder (itemKey, itemType, position)
            SELECT 'app:' || packageId, 'app', favouritePosition
            FROM modifiedApps
            WHERE favouritePosition IS NOT NULL
            """.trimIndent()
        )
    }
}

/**
 * Adds per-owner "related items" storage: a user-configurable, ordered list of other apps/
 * shortcuts shown as quick-launch entries in a given app or shortcut's long-press popup.
 * Wholly new concept, nothing to backfill.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `relatedItems` (
                `ownerItemKey` TEXT NOT NULL,
                `relatedItemKey` TEXT NOT NULL,
                `relatedItemType` TEXT NOT NULL,
                `position` REAL NOT NULL,
                PRIMARY KEY(`ownerItemKey`, `relatedItemKey`)
            )
            """.trimIndent()
        )
    }
}
