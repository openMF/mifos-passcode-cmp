package com.mifos.passcode.auth.deviceAuth


import com.mifos.passcode.auth.deviceAuth.presentation.AuthenticationResult
import com.mifos.passcode.auth.deviceAuth.presentation.AuthenticatorStatus


expect class PlatformAuthenticator private constructor(){

    constructor(activity: Any?)
    fun getDeviceAuthenticatorStatus(): AuthenticatorStatus

    fun setDeviceAuthOption()

    suspend fun authenticate(title: String = ""): AuthenticationResult

}