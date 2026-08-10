package com.example.uos_lms.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.uos_lms.core.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class CachedSession(val uid: String, val role: UserRole, val fullName: String)

private object SessionKeys {
    val UID = stringPreferencesKey("uid")
    val ROLE = stringPreferencesKey("role")
    val FULL_NAME = stringPreferencesKey("full_name")
}

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val cachedSession: Flow<CachedSession?> = dataStore.data.map { prefs ->
        val uid = prefs[SessionKeys.UID] ?: return@map null
        val role = UserRole.fromStringOrNull(prefs[SessionKeys.ROLE]) ?: return@map null
        CachedSession(uid = uid, role = role, fullName = prefs[SessionKeys.FULL_NAME].orEmpty())
    }

    suspend fun cache(uid: String, role: UserRole, fullName: String) {
        dataStore.edit { prefs ->
            prefs[SessionKeys.UID] = uid
            prefs[SessionKeys.ROLE] = role.name
            prefs[SessionKeys.FULL_NAME] = fullName
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
