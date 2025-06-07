package com.mifos.passcode.auth.deviceAuth


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)

    fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun registerUser():  Pair<AuthenticationResult, String>

    suspend fun authenticate(title: String = "",savedRegistrationOutput: String?): AuthenticationResult
}