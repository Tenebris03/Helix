package com.tenebris.health_tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tenebris.health_tracker.data.model.ProfileEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile_entries WHERE date <= :date ORDER BY date DESC, id DESC LIMIT 1")
    fun getProfileAtDate(date: String): Flow<ProfileEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(entry: ProfileEntry)

    @Query("SELECT * FROM profile_entries ORDER BY date DESC, id DESC LIMIT 1")
    fun getLatestProfile(): Flow<ProfileEntry?>
}
