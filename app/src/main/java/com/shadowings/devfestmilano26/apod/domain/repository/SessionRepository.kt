package com.shadowings.devfestmilano26.apod.domain.repository

import com.shadowings.devfestmilano26.apod.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSession(): Flow<UserSession>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(): Result<Unit>
    suspend fun continueWithoutAccount()
    suspend fun signOut()
}
