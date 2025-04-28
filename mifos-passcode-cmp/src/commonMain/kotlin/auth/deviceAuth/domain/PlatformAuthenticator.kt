package com.mifos.passcode.auth.deviceAuth.domain

import com.mifos.passcode.biometric.domain.AuthenticatorStatus


interface PlatformAuthenticator {

    fun getDeviceAuthenticatorStatus(): AuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun authenticate(): AuthenticationResult
}

