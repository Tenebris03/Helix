package com.tenebris.health_tracker.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val TDEE = intPreferencesKey("tdee") // This will now act as a fallback or be updated
        val GOAL = stringPreferencesKey("goal")
        val OFFSET = intPreferencesKey("offset")
        val PROTEIN_TARGET = intPreferencesKey("protein_target")
        val ACTIVITY_LEVEL = floatPreferencesKey("activity_level")
        
        // New keys to preserve profile for dynamic BMR
        val GENDER = stringPreferencesKey("gender")
        val AGE = intPreferencesKey("age")
        val HEIGHT = intPreferencesKey("height")
    }

    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[IS_ONBOARDED] ?: false }
    val tdee: Flow<Int> = context.dataStore.data.map { it[TDEE] ?: 2000 }
    val goal: Flow<String> = context.dataStore.data.map { it[GOAL] ?: "Maintain" }
    val offset: Flow<Int> = context.dataStore.data.map { it[OFFSET] ?: 0 }
    val proteinTarget: Flow<Int> = context.dataStore.data.map { it[PROTEIN_TARGET] ?: 150 }
    val activityLevel: Flow<Float> = context.dataStore.data.map { it[ACTIVITY_LEVEL] ?: 1.2f }
    
    val gender: Flow<String> = context.dataStore.data.map { it[GENDER] ?: "Male" }
    val age: Flow<Int> = context.dataStore.data.map { it[AGE] ?: 25 }
    val height: Flow<Int> = context.dataStore.data.map { it[HEIGHT] ?: 170 }

    suspend fun saveOnboardingData(
        tdee: Int, 
        goal: String, 
        offset: Int, 
        proteinTarget: Int, 
        activityLevel: Float,
        gender: String,
        age: Int,
        height: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[IS_ONBOARDED] = true
            prefs[TDEE] = tdee
            prefs[GOAL] = goal
            prefs[OFFSET] = offset
            prefs[PROTEIN_TARGET] = proteinTarget
            prefs[ACTIVITY_LEVEL] = activityLevel
            prefs[GENDER] = gender
            prefs[AGE] = age
            prefs[HEIGHT] = height
        }
    }
}
