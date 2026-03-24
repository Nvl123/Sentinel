package com.dicoding.sentinel.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sentinel_prefs")

class SentinelPreference(private val context: Context) {

    companion object {
        private val STREAK_START_KEY = longPreferencesKey("streak_start_time")
        private val LONGEST_STREAK_KEY = longPreferencesKey("longest_streak")
        private val URGE_DEFEATED_COUNT = longPreferencesKey("urge_defeated_count")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val ACTIVE_PROTOCOL_ID = longPreferencesKey("active_protocol_id")
        private val FIREWALL_START_TIME = longPreferencesKey("firewall_start_time")
        private val USED_PROTOCOL_IDS = longPreferencesKey("used_protocol_ids") // Stored as bitmask or comma string, but let's use a string for simplicity
        private val USED_PROTOCOL_IDS_STR = androidx.datastore.preferences.core.stringPreferencesKey("used_protocol_ids_str")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val LOCKED_APPS_STR = androidx.datastore.preferences.core.stringPreferencesKey("locked_apps_str")
    }

    val streakStartTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[STREAK_START_KEY]
    }

    val longestStreak: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LONGEST_STREAK_KEY] ?: 0L
    }

    val urgeDefeatedCount: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[URGE_DEFEATED_COUNT] ?: 0L
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val activeProtocolId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_PROTOCOL_ID]?.toInt()
    }

    val usedProtocolIdsStr: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USED_PROTOCOL_IDS_STR] ?: ""
    }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_ENABLED] ?: false
    }

    val lockedAppsStr: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LOCKED_APPS_STR] ?: ""
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
        }
    }

    suspend fun resetStreak() {
        context.dataStore.edit { preferences ->
            val start = preferences[STREAK_START_KEY] ?: System.currentTimeMillis()
            val currentStreak = System.currentTimeMillis() - start
            val maxStreak = preferences[LONGEST_STREAK_KEY] ?: 0L
            if (currentStreak > maxStreak) {
                preferences[LONGEST_STREAK_KEY] = currentStreak
            }
            preferences[STREAK_START_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun initializeStreak() {
        context.dataStore.edit { preferences ->
            if (preferences[STREAK_START_KEY] == null) {
                preferences[STREAK_START_KEY] = System.currentTimeMillis()
            }
        }
    }

    suspend fun incrementUrgeDefeated() {
        context.dataStore.edit { preferences ->
            val current = preferences[URGE_DEFEATED_COUNT] ?: 0L
            preferences[URGE_DEFEATED_COUNT] = current + 1
        }
    }

    suspend fun saveFirewallState(protocolId: Int, usedIds: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_PROTOCOL_ID] = protocolId.toLong()
            preferences[USED_PROTOCOL_IDS_STR] = usedIds
        }
    }

    suspend fun clearFirewallState() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_PROTOCOL_ID)
            preferences.remove(USED_PROTOCOL_IDS_STR)
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.remove(STREAK_START_KEY)
            preferences.remove(LONGEST_STREAK_KEY)
            preferences.remove(URGE_DEFEATED_COUNT)
            preferences.remove(ACTIVE_PROTOCOL_ID)
            preferences.remove(USED_PROTOCOL_IDS_STR)
            preferences.remove(APP_LOCK_ENABLED)
            preferences.remove(LOCKED_APPS_STR)
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setLockedApps(packageNames: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCKED_APPS_STR] = packageNames
        }
    }
}
