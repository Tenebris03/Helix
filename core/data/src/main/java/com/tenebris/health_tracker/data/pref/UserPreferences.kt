package com.tenebris.health_tracker.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(
    private val context: Context,
) {
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

        // Coach throttling
        val LAST_COACH_INTERVENTION = longPreferencesKey("last_coach_intervention")
        val COACH_HEADLINE = stringPreferencesKey("coach_headline")
        val COACH_BODY = stringPreferencesKey("coach_body")
        val COACH_API_KEY_VALID = booleanPreferencesKey("coach_api_key_valid")
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

    val lastCoachIntervention: Flow<Long> = context.dataStore.data.map { it[LAST_COACH_INTERVENTION] ?: 0L }
    val coachHeadline: Flow<String?> = context.dataStore.data.map { it[COACH_HEADLINE] }
    val coachBody: Flow<String?> = context.dataStore.data.map { it[COACH_BODY] }
    val coachApiKeyValid: Flow<Boolean> = context.dataStore.data.map { it[COACH_API_KEY_VALID] ?: true }

    data class PrefsSnapshot(
        val goal: String,
        val offset: Int,
        val proteinTarget: Int,
        val activityLevel: Float,
        val gender: String,
        val age: Int,
        val height: Int,
    )

    val snapshot: Flow<PrefsSnapshot> =
        context.dataStore.data.map { prefs ->
            PrefsSnapshot(
                goal = prefs[GOAL] ?: "Maintain",
                offset = prefs[OFFSET] ?: 0,
                proteinTarget = prefs[PROTEIN_TARGET] ?: 150,
                activityLevel = prefs[ACTIVITY_LEVEL] ?: 1.2f,
                gender = prefs[GENDER] ?: "Male",
                age = prefs[AGE] ?: 25,
                height = prefs[HEIGHT] ?: 170,
            )
        }

    suspend fun saveCoachInterventionTimestamp() {
        context.dataStore.edit { prefs ->
            prefs[LAST_COACH_INTERVENTION] = System.currentTimeMillis()
        }
    }

    suspend fun clearCoachInterventionTimestamp() {
        context.dataStore.edit { prefs ->
            prefs.remove(LAST_COACH_INTERVENTION)
        }
    }

    suspend fun saveCoachResponse(
        headline: String,
        body: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[COACH_HEADLINE] = headline
            prefs[COACH_BODY] = body
        }
    }

    suspend fun clearCoachResponse() {
        context.dataStore.edit { prefs ->
            prefs.remove(COACH_HEADLINE)
            prefs.remove(COACH_BODY)
        }
    }

    suspend fun setCoachApiKeyValid(valid: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[COACH_API_KEY_VALID] = valid
        }
    }

    suspend fun saveOnboardingData(
        tdee: Int,
        goal: String,
        offset: Int,
        proteinTarget: Int,
        activityLevel: Float,
        gender: String,
        age: Int,
        height: Int,
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
