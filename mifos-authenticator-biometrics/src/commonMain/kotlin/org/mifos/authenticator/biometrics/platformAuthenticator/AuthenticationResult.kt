package org.mifos.authenticator.biometrics.platformAuthenticator

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Error(val message: String) : AuthenticationResult
    data object UserNotRegistered : AuthenticationResult
}