package com.geecee.escapelauncher.core.model

data class RelatedItem(
    val ownerItemKey: String,
    val relatedItemKey: String,
    val relatedItemType: String,
    val position: Double
)
