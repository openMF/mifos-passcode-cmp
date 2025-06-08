package com.mifos.passcode.auth.deviceAuth

import kotlinx.serialization.Serializable

sealed class AuthenticationResult {
    @Serializable data class Success(val message: String = "Success"): AuthenticationResult()
    @Serializable data class Failed(val message: String = "Failed"): AuthenticationResult()
    @Serializable data class Error(val message: String): AuthenticationResult()
    @Serializable data object RegisterAgain: AuthenticationResult()
    @Serializable data object PlatformAuthenticatorNotSet: AuthenticationResult()
}