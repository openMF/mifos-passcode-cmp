package com.mifos.passcode.demoImpl

import com.mifos.passcode.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.biometric.domain.AuthenticatorStatus
import com.mifos.passcode.deviceAuth.domain.PlatformAuthenticator

class DemoAuthenticator: PlatformAuthenticator {

    override fun getDeviceAuthenticatorStatus(): AuthenticatorStatus {
        return AuthenticatorStatus(
            userCredentialSet = false,
            biometricsNotPossible = true,
            biometricsSet = false,
            message = "Coming Soon"
        )
    }

    override fun setDeviceAuthOption() {}

    override suspend fun authenticate(): AuthenticationResult {
        return AuthenticationResult.Error("Coming Soon")
    }

}

