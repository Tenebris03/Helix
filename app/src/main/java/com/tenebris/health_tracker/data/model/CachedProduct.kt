package com.tenebris.health_tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_products")
data class CachedProduct(
    @PrimaryKey
    val barcode: String,
    val name: String,
    val calories100g: Int,
    val protein100g: Int,
    val lastAccessed: Long = System.currentTimeMillis()
)
