package com.shadowings.devfestmilano26.apod.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shadowings.devfestmilano26.apod.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
private val UID = stringPreferencesKey("uid")
private val EMAIL = stringPreferencesKey("email")

/**
 * Preferences DataStore-backed cache of [UserSession] -- never tokens or credentials (only
 * isSignedIn/uid/email, per research.md "Secrets handling").
 */
@Singleton
class SessionPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun observe(): Flow<UserSession> = dataStore.data.map { prefs ->
        val isSignedIn = prefs[IS_SIGNED_IN] ?: false
        if (isSignedIn) {
            UserSession(isSignedIn = true, uid = prefs[UID], email = prefs[EMAIL])
        } else {
            UserSession.SignedOut
        }
    }

    suspend fun save(uid: String, email: String) {
        dataStore.edit { prefs ->
            prefs[IS_SIGNED_IN] = true
            prefs[UID] = uid
            prefs[EMAIL] = email
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs[IS_SIGNED_IN] = false
            prefs.remove(UID)
            prefs.remove(EMAIL)
        }
    }
}
