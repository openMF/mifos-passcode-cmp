package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult


actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()
    actual fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus {
        return PlatformAuthenticatorStatus.UnsupportedPlatform()
    }

    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(): RegistrationResult {
        return RegistrationResult.PlatformAuthenticatorNotSet
    }

    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {
        return AuthenticationResult.UserNotRegistered    }
}