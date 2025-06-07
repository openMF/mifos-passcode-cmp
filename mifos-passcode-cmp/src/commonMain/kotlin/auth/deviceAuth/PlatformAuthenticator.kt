package com.mifos.passcode.auth.deviceAuth


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)

    fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun registerUser():  AuthenticationResult

    suspend fun authenticate(title: String = "",savedRegistrationOutput: String?): AuthenticationResult
}