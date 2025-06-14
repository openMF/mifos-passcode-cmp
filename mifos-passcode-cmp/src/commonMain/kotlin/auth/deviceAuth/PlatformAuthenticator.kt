package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any? = null)

    fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus>

    fun setDeviceAuthOption()

    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult

    suspend fun authenticate(title: String = "",savedRegistrationOutput: String?): AuthenticationResult
}