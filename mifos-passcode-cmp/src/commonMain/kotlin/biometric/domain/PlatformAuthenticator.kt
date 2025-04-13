package com.mifos.passcode.biometric.domain


interface PlatformAuthenticator {

    fun canAuthenticate(): AuthenticatorStatus

    fun setAuthOption()

    suspend fun authenticate(): AuthenticationResult
}

