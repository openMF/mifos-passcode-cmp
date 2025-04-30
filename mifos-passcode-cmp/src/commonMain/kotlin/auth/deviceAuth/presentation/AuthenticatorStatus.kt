package com.mifos.passcode.auth.deviceAuth.presentation

data class AuthenticatorStatus(
    var biometricsSet: Boolean = false,
    var message: String = "",
    var biometricsNotPossible: Boolean = true,
    var userCredentialSet: Boolean = false
)