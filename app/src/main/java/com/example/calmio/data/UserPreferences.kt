package com.example.calmio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calmio_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val DARK_MODE_KEY        = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_KEY    = booleanPreferencesKey("notifications_enabled")
        val AVATAR_INDEX_KEY     = intPreferencesKey("avatar_index")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    val notificationsFlow: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_KEY] ?: true }

    val avatarIndexFlow: Flow<Int> = context.dataStore.data
        .map { it[AVATAR_INDEX_KEY] ?: 0 }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
    }

    suspend fun setAvatarIndex(index: Int) {
        context.dataStore.edit { it[AVATAR_INDEX_KEY] = index }
    }
}