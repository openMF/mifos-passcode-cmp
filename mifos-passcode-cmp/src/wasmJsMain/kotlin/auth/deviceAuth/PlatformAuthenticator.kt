package com.mifos.passcode.auth.deviceAuth


actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()
    actual fun getDeviceAuthenticatorStatus(): AuthenticatorStatus {
        return AuthenticatorStatus(
            userCredentialSet = false,
            biometricsNotPossible = true,
            biometricsSet = false,
            message = "Coming Soon"
        )
    }

    actual fun setDeviceAuthOption() {}

    actual suspend fun authenticate(title: String): AuthenticationResult {
        return AuthenticationResult.Error("Coming Soon")
    }

    actual suspend fun registerUser(): AuthenticationResult {
        return AuthenticationResult.Failed("Not available")
    }
}