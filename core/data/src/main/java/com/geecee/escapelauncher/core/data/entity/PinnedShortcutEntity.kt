package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity

@Entity(tableName = "pinnedShortcuts", primaryKeys = ["packageName", "shortcutId"])
data class PinnedShortcutEntity(
    val packageName: String,
    val shortcutId: String,
    val label: String,
    val customLabel: String? = null,
    val pinnedAt: Long = System.currentTimeMillis()
)
