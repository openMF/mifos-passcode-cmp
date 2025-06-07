package com.mifos.passcode.auth.deviceAuth


actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()
    actual fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus {
        return PlatformAuthenticatorStatus.UnsupportedPlatform()
    }

    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(): Pair<AuthenticationResult,String> {
        return Pair(AuthenticationResult.Success("Already setup"), "")
    }

    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {
        return AuthenticationResult.Error("Coming Soon")
    }
}