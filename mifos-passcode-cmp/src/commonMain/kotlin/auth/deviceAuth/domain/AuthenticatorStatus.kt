package com.mifos.passcode.biometric.domain

data class AuthenticatorStatus(
    var biometricsSet: Boolean = false,
    var message: String = "",
    var biometricsNotPossible: Boolean = true,
    var userCredentialSet: Boolean = false
)