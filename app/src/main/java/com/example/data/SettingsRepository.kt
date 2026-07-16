package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val CURRENT_STREAK_KEY = intPreferencesKey("current_streak")
    private val CURRENT_QUOTE_DATE_KEY = stringPreferencesKey("current_quote_date")
    private val CURRENT_QUOTE_ID_KEY = stringPreferencesKey("current_quote_id")
    private val IS_ROUTINE_EXPANDED_KEY = booleanPreferencesKey("is_routine_expanded")
    private val LAST_SYNC_TIME_KEY = stringPreferencesKey("last_sync_time")
    private val GUEST_BACKUP_KEY = stringPreferencesKey("guest_backup")

    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK_KEY] ?: 0 }
    val currentQuoteDate: Flow<String?> = context.dataStore.data.map { it[CURRENT_QUOTE_DATE_KEY] }
    val currentQuoteId: Flow<String?> = context.dataStore.data.map { it[CURRENT_QUOTE_ID_KEY] }
    val isRoutineExpanded: Flow<Boolean> = context.dataStore.data.map { it[IS_ROUTINE_EXPANDED_KEY] ?: false }
    val lastSyncTime: Flow<String?> = context.dataStore.data.map { it[LAST_SYNC_TIME_KEY] }
    val guestBackup: Flow<String?> = context.dataStore.data.map { it[GUEST_BACKUP_KEY] }

    suspend fun setCurrentStreak(streak: Int) {
        context.dataStore.edit { it[CURRENT_STREAK_KEY] = streak }
    }

    suspend fun setCurrentQuoteDate(date: String) {
        context.dataStore.edit { it[CURRENT_QUOTE_DATE_KEY] = date }
    }

    suspend fun setCurrentQuoteId(id: String) {
        context.dataStore.edit { it[CURRENT_QUOTE_ID_KEY] = id }
    }
    suspend fun setRoutineExpanded(expanded: Boolean) {
        context.dataStore.edit { it[IS_ROUTINE_EXPANDED_KEY] = expanded }
    }
    suspend fun setLastSyncTime(time: String) {
        context.dataStore.edit { it[LAST_SYNC_TIME_KEY] = time }
    }
    suspend fun setGuestBackup(json: String?) {
        context.dataStore.edit { prefs -> 
            if (json == null) prefs.remove(GUEST_BACKUP_KEY) else prefs[GUEST_BACKUP_KEY] = json 
        }
    }
}
