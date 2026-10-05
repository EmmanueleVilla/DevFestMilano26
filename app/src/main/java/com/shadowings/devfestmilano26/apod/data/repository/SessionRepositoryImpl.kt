package com.shadowings.devfestmilano26.apod.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shadowings.devfestmilano26.BuildConfig
import com.shadowings.devfestmilano26.apod.data.local.SessionPreferences
import com.shadowings.devfestmilano26.apod.domain.model.UserSession
import com.shadowings.devfestmilano26.apod.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val sessionPreferences: SessionPreferences,
) : SessionRepository {

    override fun observeSession(): Flow<UserSession> = sessionPreferences.observe()

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = requireNotNull(result.user) { "Sign-in succeeded with no user" }
            sessionPreferences.save(uid = user.uid, email = user.email.orEmpty())
        }

    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credentialManager = CredentialManager.create(context)
        val response = credentialManager.getCredential(context, request)
        val credential = response.credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Unexpected credential type: ${credential.type}" }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val result = firebaseAuth.signInWithCredential(firebaseCredential).await()
        val user = requireNotNull(result.user) { "Sign-in succeeded with no user" }
        sessionPreferences.save(uid = user.uid, email = user.email.orEmpty())
    }

    override suspend fun continueWithoutAccount() {
        // No Firebase call -- per FR-012, skipping login must not create any account.
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        sessionPreferences.clear()
    }
}
