package com.shadowings.devfestmilano26.apod.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowings.devfestmilano26.apod.domain.repository.SessionRepository
import com.shadowings.devfestmilano26.apod.domain.usecase.SyncFavoritesOnLoginUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val syncFavoritesOnLogin: SyncFavoritesOnLoginUseCase,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Default)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val effectChannel = Channel<LoginEffect>()
    val effects: Flow<LoginEffect> = effectChannel.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SubmitEmailPassword -> signIn { sessionRepository.signInWithEmail(intent.email, intent.password) }
            LoginIntent.SignInWithGoogle -> signIn { sessionRepository.signInWithGoogle() }
            LoginIntent.ContinueWithoutAccount -> continueWithoutAccount()
        }
    }

    private fun signIn(block: suspend () -> Result<Unit>) {
        _state.value = LoginState.SigningIn
        viewModelScope.launch {
            val result = block()
            result.fold(
                onSuccess = {
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) syncFavoritesOnLogin(uid)
                    _state.value = LoginState.Default
                    effectChannel.send(LoginEffect.Done)
                },
                onFailure = { error ->
                    _state.value = LoginState.Error(error.message ?: "Sign-in failed")
                },
            )
        }
    }

    private fun continueWithoutAccount() {
        viewModelScope.launch {
            sessionRepository.continueWithoutAccount()
            effectChannel.send(LoginEffect.Done)
        }
    }
}
