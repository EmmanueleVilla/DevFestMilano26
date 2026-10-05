package com.shadowings.devfestmilano26.apod.presentation.login

sealed interface LoginState {
    data object Default : LoginState
    data object SigningIn : LoginState
    data class Error(val message: String) : LoginState
}

sealed interface LoginIntent {
    data class SubmitEmailPassword(val email: String, val password: String) : LoginIntent
    data object SignInWithGoogle : LoginIntent
    data object ContinueWithoutAccount : LoginIntent
}

sealed interface LoginEffect {
    data object Done : LoginEffect
}
