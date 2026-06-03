package com.tenebris.health_tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_entries")
data class ProfileEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO-8601 format yyyy-MM-dd
    val activityLevel: Float,
    val height: Int,
    val age: Int,
    val gender: String,
    val goal: String,
    val offset: Int,
    val proteinTarget: Int,
)
