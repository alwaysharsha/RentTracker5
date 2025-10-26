package com.renttracker.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val CURRENCY_KEY = stringPreferencesKey("currency")
        private val APP_LOCK_KEY = booleanPreferencesKey("app_lock")
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "USD"
    }

    val appLockFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_KEY] ?: false
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency
        }
    }

    suspend fun setAppLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_KEY] = enabled
        }
    }
}
