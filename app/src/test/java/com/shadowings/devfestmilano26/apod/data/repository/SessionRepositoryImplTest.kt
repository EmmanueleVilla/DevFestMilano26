package com.shadowings.devfestmilano26.apod.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.shadowings.devfestmilano26.apod.data.local.SessionPreferences
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SessionRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val sessionPreferences: SessionPreferences = mockk(relaxed = true)
    private val repository = SessionRepositoryImpl(context, firebaseAuth, sessionPreferences)

    @Test
    fun `continuing without an account never calls Firebase`() = runTest {
        repository.continueWithoutAccount()

        verify { firebaseAuth wasNot io.mockk.Called }
    }

    @Test
    fun `signing out clears Firebase and the local session cache`() = runTest {
        justRun { firebaseAuth.signOut() }
        coJustRun { sessionPreferences.clear() }

        repository.signOut()

        verify { firebaseAuth.signOut() }
        coVerify { sessionPreferences.clear() }
    }
}
