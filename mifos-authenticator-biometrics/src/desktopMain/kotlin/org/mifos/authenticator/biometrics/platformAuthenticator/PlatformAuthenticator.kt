/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

import com.sun.jna.Platform
import org.mifos.authenticator.biometrics.mockServer.WindowsAuthenticationResponse
import org.mifos.authenticator.biometrics.windows.WindowsAuthenticatorResponse
import org.mifos.authenticator.biometrics.windows.WindowsHelloAuthenticator
import org.mifos.authenticator.biometrics.windows.WindowsHelloAuthenticatorNativeSupportImpl
import org.mifos.authenticator.biometrics.windows.WindowsRegistrationResponse
import org.mifos.authenticator.biometrics.windows.utils.decodeWindowsAuthenticatorFromJson
import org.mifos.authenticator.biometrics.windows.utils.encodeWindowsAuthenticatorToJsonString
import org.mifos.authenticator.biometrics.windows.utils.isWindowsTenOrEleven

actual class PlatformAuthenticator private actual constructor() {

    actual constructor(activity: Any?) : this()

    private val isWindowsTenOrHigh = if (Platform.isWindows()) isWindowsTenOrEleven() else false

    private val windowsHelloAuthenticatorNativeSupport by lazy {
        WindowsHelloAuthenticatorNativeSupportImpl()
    }

    private val windowsHelloAuthenticator by lazy {
        WindowsHelloAuthenticator(
            windowsHelloAuthenticatorNativeSupport,
        )
    }

    actual fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus> {
        if (isWindowsTenOrHigh) {
            val isWindowsHelloAvailable = windowsHelloAuthenticator.checkIfWindowsHelloSupportedOrNot()
            if (isWindowsHelloAvailable) return setOf(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
            return setOf(PlatformAuthenticatorStatus.NOT_SETUP)
        }
        return setOf(PlatformAuthenticatorStatus.NOT_AVAILABLE)
    }

    actual fun setDeviceAuthOption() {}

    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
    ): RegistrationResult {
        if (isWindowsTenOrHigh) {
            val windowsAuthResponse = windowsHelloAuthenticator.invokeUserRegistration(
                userName,
                emailId,
                displayName,
            )

            if (windowsAuthResponse is WindowsAuthenticatorResponse.Registration.Error) {
                return RegistrationResult.Error("Error while registering user")
            }
            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Registration.Success)
                .response.windowsAuthenticationResponse
            return if (response == WindowsAuthenticationResponse.SUCCESS) {
                RegistrationResult.Success(
                    encodeWindowsAuthenticatorToJsonString(
                        windowsAuthResponse.response,
                    ),
                )
            } else {
                returnRegistrationResult(windowsAuthResponse.response)
            }
        }
        return RegistrationResult.PlatformAuthenticatorNotAvailable
    }

    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {
        if (isWindowsTenOrHigh) {
            val windowsRegistrationResponse: WindowsRegistrationResponse = savedRegistrationOutput?.let {
                decodeWindowsAuthenticatorFromJson(savedRegistrationOutput)
            } ?: return AuthenticationResult.Error("Invalid registration data")

            val windowsAuthResponse: WindowsAuthenticatorResponse.Verification =
                windowsHelloAuthenticator.invokeUserVerification(
                    windowsRegistrationResponse,
                )

            if (windowsAuthResponse is WindowsAuthenticatorResponse.Verification.Error) {
                return AuthenticationResult.Error("Error while registering user")
            }

            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Verification.Success).response
            return returnAuthenticatorResult(response)
        }

        return AuthenticationResult.UserNotRegistered
    }
}

fun returnAuthenticatorResult(windowsAuthenticatorResponse: WindowsAuthenticationResponse): AuthenticationResult {
    return when (windowsAuthenticatorResponse) {
        WindowsAuthenticationResponse.SUCCESS -> AuthenticationResult.Success
        WindowsAuthenticationResponse.UNSUCCESSFUL -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR -> AuthenticationResult.Error(
            windowsAuthenticatorResponse.name,
        )
        WindowsAuthenticationResponse.E_FAILURE -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.ABORTED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.USER_CANCELED -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.REGISTER_AGAIN -> AuthenticationResult.UserNotRegistered
        WindowsAuthenticationResponse.UNKNOWN_ERROR -> AuthenticationResult.Error(windowsAuthenticatorResponse.name)
        WindowsAuthenticationResponse.INVALID_PARAMETER -> AuthenticationResult.Error(
            "${windowsAuthenticatorResponse.name}: Invalid arguments used for authentication",
        )
    }
}

fun returnRegistrationResult(windowsRegistrationResponse: WindowsRegistrationResponse): RegistrationResult {
    return when (windowsRegistrationResponse.windowsAuthenticationResponse) {
        WindowsAuthenticationResponse.SUCCESS -> RegistrationResult.Success(
            encodeWindowsAuthenticatorToJsonString(windowsRegistrationResponse),
        )
        WindowsAuthenticationResponse.UNSUCCESSFUL -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.E_FAILURE -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.ABORTED -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.USER_CANCELED -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.REGISTER_AGAIN -> RegistrationResult.PlatformAuthenticatorNotSet
        WindowsAuthenticationResponse.UNKNOWN_ERROR -> RegistrationResult.Error(
            windowsRegistrationResponse.windowsAuthenticationResponse.name,
        )
        WindowsAuthenticationResponse.INVALID_PARAMETER -> RegistrationResult.Error(
            "${windowsRegistrationResponse.windowsAuthenticationResponse.name}: " +
                "Invalid arguments used for registration.",
        )
    }
}
