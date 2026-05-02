/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
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
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
    ): RegistrationResult {
        // Windows Hello does not consume title/subtitle/description/negativeButtonText —
        // the system prompt UI is OS-localized; consumer strings are ignored here.
        if (isWindowsTenOrHigh) {
            val windowsAuthResponse = windowsHelloAuthenticator.invokeUserRegistration(
                userName,
                emailId,
                displayName,
            )

            when (windowsAuthResponse) {
                is WindowsAuthenticatorResponse.Registration.UserCancelled ->
                    return RegistrationResult.UserCancelled
                is WindowsAuthenticatorResponse.Registration.Error ->
                    return RegistrationResult.Error(BiometricError.Unknown())
                is WindowsAuthenticatorResponse.Registration.Success -> {
                    val response = windowsAuthResponse.response.windowsAuthenticationResponse
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
            }
        }
        return RegistrationResult.PlatformAuthenticatorNotAvailable
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
        savedRegistrationOutput: String?,
    ): AuthenticationResult {
        if (isWindowsTenOrHigh) {
            val windowsRegistrationResponse: WindowsRegistrationResponse = savedRegistrationOutput?.let {
                decodeWindowsAuthenticatorFromJson(savedRegistrationOutput)
            } ?: return AuthenticationResult.Error(
                BiometricError.Unknown(platformMessage = "Invalid registration data"),
            )

            val windowsAuthResponse: WindowsAuthenticatorResponse.Verification =
                windowsHelloAuthenticator.invokeUserVerification(
                    windowsRegistrationResponse,
                )

            when (windowsAuthResponse) {
                is WindowsAuthenticatorResponse.Verification.UserCancelled ->
                    return AuthenticationResult.UserCancelled
                is WindowsAuthenticatorResponse.Verification.Error ->
                    return AuthenticationResult.Error(BiometricError.Unknown())
                is WindowsAuthenticatorResponse.Verification.Success ->
                    return returnAuthenticatorResult(windowsAuthResponse.response)
            }
        }

        return AuthenticationResult.UserNotRegistered
    }
}

fun returnAuthenticatorResult(windowsAuthenticatorResponse: WindowsAuthenticationResponse): AuthenticationResult {
    return when (windowsAuthenticatorResponse) {
        WindowsAuthenticationResponse.SUCCESS -> AuthenticationResult.Success
        WindowsAuthenticationResponse.UNSUCCESSFUL,
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR,
        WindowsAuthenticationResponse.E_FAILURE,
        WindowsAuthenticationResponse.ABORTED,
        WindowsAuthenticationResponse.UNKNOWN_ERROR,
        -> AuthenticationResult.Error(
            BiometricError.Unknown(platformMessage = windowsAuthenticatorResponse.name),
        )
        // Already handled via Verification.UserCancelled upstream; required for exhaustive when.
        WindowsAuthenticationResponse.USER_CANCELED -> AuthenticationResult.UserCancelled
        WindowsAuthenticationResponse.REGISTER_AGAIN -> AuthenticationResult.UserNotRegistered
        WindowsAuthenticationResponse.INVALID_PARAMETER -> AuthenticationResult.Error(
            BiometricError.Unknown(
                platformMessage = "${windowsAuthenticatorResponse.name}: Invalid arguments used for authentication",
            ),
        )
    }
}

fun returnRegistrationResult(windowsRegistrationResponse: WindowsRegistrationResponse): RegistrationResult {
    val response = windowsRegistrationResponse.windowsAuthenticationResponse
    return when (response) {
        WindowsAuthenticationResponse.SUCCESS -> RegistrationResult.Success(
            encodeWindowsAuthenticatorToJsonString(windowsRegistrationResponse),
        )
        WindowsAuthenticationResponse.UNSUCCESSFUL,
        WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR,
        WindowsAuthenticationResponse.E_FAILURE,
        WindowsAuthenticationResponse.ABORTED,
        WindowsAuthenticationResponse.UNKNOWN_ERROR,
        -> RegistrationResult.Error(BiometricError.Unknown(platformMessage = response.name))
        // Already handled via Registration.UserCancelled upstream; required for exhaustive when.
        WindowsAuthenticationResponse.USER_CANCELED -> RegistrationResult.UserCancelled
        WindowsAuthenticationResponse.REGISTER_AGAIN -> RegistrationResult.PlatformAuthenticatorNotSet
        WindowsAuthenticationResponse.INVALID_PARAMETER -> RegistrationResult.Error(
            BiometricError.Unknown(platformMessage = "${response.name}: Invalid arguments used for registration."),
        )
    }
}
