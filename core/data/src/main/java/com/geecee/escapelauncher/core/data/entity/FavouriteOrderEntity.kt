package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Shared favourite ordering for both apps and pinned shortcuts, keyed by
 * [com.geecee.escapelauncher.core.model.LauncherItem.itemKey] (e.g. "app:<packageName>" or
 * "shortcut:<packageName>:<shortcutId>"). Using one shared, fractional-position sequence for both
 * item types is what lets them be freely interleaved and drag-reordered together on the home
 * screen, rather than apps and shortcuts each having their own independent order.
 */
@Entity(tableName = "favouriteOrder")
data class FavouriteOrderEntity(
    @PrimaryKey val itemKey: String,
    val itemType: String,
    val position: Double
)
