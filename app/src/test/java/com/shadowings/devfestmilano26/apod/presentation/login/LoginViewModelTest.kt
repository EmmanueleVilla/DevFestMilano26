package com.shadowings.devfestmilano26.apod.presentation.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shadowings.devfestmilano26.apod.domain.model.UserSession
import com.shadowings.devfestmilano26.apod.domain.repository.SessionRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.FakeFavoritesRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.SyncFavoritesOnLoginUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeSessionRepository(
    private val signInResult: Result<Unit> = Result.success(Unit),
    var continuedWithoutAccount: Boolean = false,
) : SessionRepository {
    override fun observeSession(): Flow<UserSession> = flowOf(UserSession.SignedOut)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = signInResult
    override suspend fun signInWithGoogle(): Result<Unit> = signInResult
    override suspend fun continueWithoutAccount() { continuedWithoutAccount = true }
    override suspend fun signOut() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun firebaseAuthWithUid(uid: String?): FirebaseAuth {
        val auth = mockk<FirebaseAuth>()
        if (uid == null) {
            every { auth.currentUser } returns null
        } else {
            val user = mockk<FirebaseUser>()
            every { user.uid } returns uid
            every { auth.currentUser } returns user
        }
        return auth
    }

    @Test
    fun `invalid credentials surface an error state`() = runTest {
        val sessionRepository = FakeSessionRepository(signInResult = Result.failure(RuntimeException("bad creds")))
        val favoritesRepository = FakeFavoritesRepository()
        val viewModel = LoginViewModel(
            sessionRepository,
            SyncFavoritesOnLoginUseCase(favoritesRepository),
            firebaseAuthWithUid(null),
        )

        viewModel.onIntent(LoginIntent.SubmitEmailPassword("a@b.com", "wrong"))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is LoginState.Error)
    }

    @Test
    fun `a successful sign-in triggers favorites migration`() = runTest {
        val sessionRepository = FakeSessionRepository()
        val favoritesRepository = FakeFavoritesRepository()
        val viewModel = LoginViewModel(
            sessionRepository,
            SyncFavoritesOnLoginUseCase(favoritesRepository),
            firebaseAuthWithUid("uid-1"),
        )

        viewModel.onIntent(LoginIntent.SubmitEmailPassword("a@b.com", "correct"))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is LoginState.Default)
    }

    @Test
    fun `continuing without an account never calls Firebase`() = runTest {
        val sessionRepository = FakeSessionRepository()
        val favoritesRepository = FakeFavoritesRepository()
        val viewModel = LoginViewModel(
            sessionRepository,
            SyncFavoritesOnLoginUseCase(favoritesRepository),
            firebaseAuthWithUid(null),
        )

        viewModel.onIntent(LoginIntent.ContinueWithoutAccount)
        testScheduler.advanceUntilIdle()

        assertTrue(sessionRepository.continuedWithoutAccount)
    }
}
