package com.mifos.passcode.auth.demoImpl

import com.mifos.passcode.auth.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.biometric.domain.AuthenticatorStatus

//class DemoAuthenticator: PlatformAuthenticator {
//
//    override fun getDeviceAuthenticatorStatus(): AuthenticatorStatus {
//        return AuthenticatorStatus(
//            userCredentialSet = false,
//            biometricsNotPossible = true,
//            biometricsSet = false,
//            message = "Coming Soon"
//        )
//    }
//
//    override fun setDeviceAuthOption() {}
//
//    override suspend fun authenticate(title: String): AuthenticationResult {
//        return AuthenticationResult.Error("Coming Soon")
//    }
//
//}
//
