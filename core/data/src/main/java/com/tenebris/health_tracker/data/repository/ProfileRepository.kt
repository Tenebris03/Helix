package com.tenebris.health_tracker.data.repository

import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.model.ProfileEntry
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val profileDao: ProfileDao,
) {
    fun getProfileAtDate(date: String): Flow<ProfileEntry?> = profileDao.getProfileAtDate(date)

    fun getLatestProfile(): Flow<ProfileEntry?> = profileDao.getLatestProfile()

    suspend fun upsertProfile(entry: ProfileEntry) = profileDao.insertProfile(entry)
}
