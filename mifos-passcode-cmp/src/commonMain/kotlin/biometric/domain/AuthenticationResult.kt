package com.mifos.passcode.biometric.domain

sealed class AuthenticationResult {
    data object Success: AuthenticationResult()
    data object Failed: AuthenticationResult()
//    data object AttemptExhausted: AuthenticationResult()
//    data object NegativeButtonClick: AuthenticationResult()
    data class Error(val error: String): AuthenticationResult()
}