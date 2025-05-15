package com.mifos.passcode.auth.deviceAuth


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)
    fun getDeviceAuthenticatorStatus(): AuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun authenticate(title: String = ""): AuthenticationResult

}