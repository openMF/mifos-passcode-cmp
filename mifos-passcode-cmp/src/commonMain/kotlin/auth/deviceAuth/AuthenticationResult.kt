package com.mifos.passcode.auth.deviceAuth

sealed class AuthenticationResult {
    data class Success(val message: String = "Success"): AuthenticationResult()
    data class Failed(val message: String = "Failed"): AuthenticationResult()
//    data object AttemptExhausted: AuthenticationResult()
//    data object NegativeButtonClick: AuthenticationResult()
    data class Error(val message: String): AuthenticationResult()
}