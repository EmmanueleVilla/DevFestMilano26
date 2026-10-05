package com.shadowings.devfestmilano26.apod.domain.model

/**
 * [uid]/[email] are always null together with [isSignedIn] == false, and both non-null when
 * [isSignedIn] == true. A read-through cache of Firebase Auth's own state -- never the source of
 * truth for whether a request is authorized.
 */
data class UserSession(
    val isSignedIn: Boolean,
    val uid: String?,
    val email: String?,
) {
    init {
        require(isSignedIn == (uid != null && email != null)) {
            "uid/email must both be non-null iff isSignedIn is true"
        }
    }

    companion object {
        val SignedOut = UserSession(isSignedIn = false, uid = null, email = null)
    }
}
