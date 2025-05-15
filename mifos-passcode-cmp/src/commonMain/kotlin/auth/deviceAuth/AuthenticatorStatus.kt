package com.mifos.passcode.auth.deviceAuth

data class AuthenticatorStatus(
    var biometricsSet: Boolean = false,
    var message: String = "",
    var biometricsNotPossible: Boolean = true,
    var userCredentialSet: Boolean = false
)