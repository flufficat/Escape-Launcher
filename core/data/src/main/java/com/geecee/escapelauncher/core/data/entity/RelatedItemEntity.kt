package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity

@Entity(tableName = "relatedItems", primaryKeys = ["ownerItemKey", "relatedItemKey"])
data class RelatedItemEntity(
    val ownerItemKey: String,
    val relatedItemKey: String,
    val relatedItemType: String,
    val position: Double
)
