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
        private val PAYMENT_METHODS_KEY = stringPreferencesKey("payment_methods")
        
        // Default payment methods
        const val DEFAULT_PAYMENT_METHODS = "UPI,Cash,Bank Transfer - Personal,Bank Transfer - HUF,Bank Transfer - Others"
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "USD"
    }

    val appLockFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCK_KEY] ?: false
    }

    val paymentMethodsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val methodsString = preferences[PAYMENT_METHODS_KEY] ?: DEFAULT_PAYMENT_METHODS
        methodsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
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

    suspend fun setPaymentMethods(methods: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PAYMENT_METHODS_KEY] = methods.joinToString(",")
        }
    }
}
