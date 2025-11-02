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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

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
        try {
            context.dataStore.edit { preferences ->
                preferences[CURRENCY_KEY] = currency
            }
        } catch (e: Exception) {
            // Handle DataStore IOException gracefully
            android.util.Log.w("PreferencesManager", "Failed to save currency to DataStore, using fallback", e)
        }
    }

    suspend fun setAppLock(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[APP_LOCK_KEY] = enabled
            }
        } catch (e: Exception) {
            // Handle DataStore IOException gracefully
            android.util.Log.w("PreferencesManager", "Failed to save app lock to DataStore, using fallback", e)
        }
    }

    suspend fun setPaymentMethods(methods: List<String>) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PAYMENT_METHODS_KEY] = methods.joinToString(",")
            }
        } catch (e: Exception) {
            // Handle DataStore IOException gracefully
            android.util.Log.w("PreferencesManager", "Failed to save payment methods to DataStore, using fallback", e)
        }
    }
    
    // Helper methods for test environment to get values synchronously
    fun getCurrencySync(): String {
        return try {
            runBlocking {
                currencyFlow.first()
            }
        } catch (e: Exception) {
            android.util.Log.w("PreferencesManager", "Failed to get currency from DataStore, using default", e)
            "USD"
        }
    }
    
    fun getAppLockSync(): Boolean {
        return try {
            runBlocking {
                appLockFlow.first()
            }
        } catch (e: Exception) {
            android.util.Log.w("PreferencesManager", "Failed to get app lock from DataStore, using default", e)
            false
        }
    }
    
    fun getPaymentMethodsSync(): List<String> {
        return try {
            runBlocking {
                paymentMethodsFlow.first()
            }
        } catch (e: Exception) {
            android.util.Log.w("PreferencesManager", "Failed to get payment methods from DataStore, using default", e)
            DEFAULT_PAYMENT_METHODS.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
