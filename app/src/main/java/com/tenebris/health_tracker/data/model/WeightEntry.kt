package com.tenebris.health_tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,
    val date: String, // ISO-8601 format yyyy-MM-dd
)
