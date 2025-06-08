package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)

    fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun registerUser(): RegistrationResult

    suspend fun authenticate(title: String = "",savedRegistrationOutput: String?): AuthenticationResult
}