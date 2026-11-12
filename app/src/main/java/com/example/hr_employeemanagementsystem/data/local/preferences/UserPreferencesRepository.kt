package com.example.hr_employeemanagementsystem.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_IS_ADMIN = booleanPreferencesKey("is_admin")
        val KEY_PIN = stringPreferencesKey("pin")
        val KEY_SELECTED_COMPANY = stringPreferencesKey("selected_company")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_CHECK_IN_DELAY = intPreferencesKey("check_in_delay")
        val KEY_CHECK_OUT_DELAY = intPreferencesKey("check_out_delay")
        val KEY_IS_KIOSK_MODE = booleanPreferencesKey("is_kiosk_mode")
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[KEY_USERNAME] ?: "" }
    val userIdFlow: Flow<Long> = context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] ?: 1L }
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_IS_LOGGED_IN] ?: false }
    val isAdminFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_IS_ADMIN] ?: false }
    val pinFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_PIN] }
    val selectedCompanyFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[KEY_SELECTED_COMPANY] ?: "Main Branch" }
    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_SOUND_ENABLED] ?: true }
    val checkInDelayFlow: Flow<Int> = context.dataStore.data.map { prefs -> prefs[KEY_CHECK_IN_DELAY] ?: 5 }
    val checkOutDelayFlow: Flow<Int> = context.dataStore.data.map { prefs -> prefs[KEY_CHECK_OUT_DELAY] ?: 5 }
    val isKioskModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_IS_KIOSK_MODE] ?: false }

    suspend fun saveLoginSession(username: String, userId: Long, isAdmin: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_USER_ID] = userId
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_IS_ADMIN] = isAdmin
        }
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN] = pin
        }
    }

    suspend fun saveCompany(company: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_COMPANY] = company
        }
    }

    suspend fun updateSettings(soundEnabled: Boolean, checkInDelay: Int, checkOutDelay: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = soundEnabled
            prefs[KEY_CHECK_IN_DELAY] = checkInDelay
            prefs[KEY_CHECK_OUT_DELAY] = checkOutDelay
        }
    }

    suspend fun setKioskMode(isKiosk: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_KIOSK_MODE] = isKiosk
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_USERNAME] = ""
            prefs[KEY_USER_ID] = 0L
            prefs[KEY_IS_ADMIN] = false
            prefs[KEY_PIN] = ""
        }
    }
}
