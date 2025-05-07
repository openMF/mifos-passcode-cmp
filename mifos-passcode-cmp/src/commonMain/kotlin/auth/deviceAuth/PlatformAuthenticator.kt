package com.mifos.passcode.auth.deviceAuth

import com.mifos.passcode.auth.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.biometric.domain.AuthenticatorStatus


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)
    fun getDeviceAuthenticatorStatus(): AuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun authenticate(title: String = ""): AuthenticationResult

}