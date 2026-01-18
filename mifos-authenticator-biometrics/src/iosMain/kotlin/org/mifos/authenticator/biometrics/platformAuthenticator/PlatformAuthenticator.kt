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

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorBiometryLockout
import platform.LocalAuthentication.LAErrorBiometryNotAvailable
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorPasscodeNotSet
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

actual class PlatformAuthenticator private actual constructor() {

    actual constructor(activity: Any?) : this()

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus> {
        val result = mutableSetOf<PlatformAuthenticatorStatus>()

        val context = LAContext()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val supportsBiometrics = context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error.ptr,
            )

            val errorCode = error.value?.code

            // Handle biometric capability and enrollment status
            if (supportsBiometrics) {
                result.add(PlatformAuthenticatorStatus.BIOMETRICS_SET)
            } else {
                when (errorCode) {
                    LAErrorBiometryNotEnrolled -> result.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_SET)
                    LAErrorBiometryNotAvailable -> result.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
                    LAErrorBiometryLockout -> result.add(PlatformAuthenticatorStatus.BIOMETRICS_UNAVAILABLE)
                    LAErrorPasscodeNotSet -> result.add(PlatformAuthenticatorStatus.NOT_SETUP)
                    else -> result.add(PlatformAuthenticatorStatus.NOT_AVAILABLE)
                }
            }

            // Check for device credentials (e.g. passcode)
            val supportsDeviceCredentials = context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthentication,
                null,
            )
            if (supportsDeviceCredentials) {
                result.add(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
            }
        }

        // Return either the accurate status or fallback
        return if (result.isEmpty()) {
            setOf(PlatformAuthenticatorStatus.NOT_AVAILABLE)
        } else {
            result
        }
    }

    actual fun setDeviceAuthOption() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        url?.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }

    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
    ): RegistrationResult {
        return when (val result = authenticate("Register yourself", null)) {
            is AuthenticationResult.Success -> RegistrationResult.Success("")
            is AuthenticationResult.Error -> RegistrationResult.Error(result.message)
            is AuthenticationResult.UserNotRegistered -> RegistrationResult.PlatformAuthenticatorNotSet
        }
    }

    actual suspend fun authenticate(
        title: String,
        savedRegistrationOutput: String?,
    ): AuthenticationResult = suspendCancellableCoroutine { continuation ->

        val context = LAContext()
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            title,
        ) { success, error ->
            if (success) {
                continuation.resume(AuthenticationResult.Success)
            } else {
                val message = error?.localizedDescription ?: "Authentication failed."
                continuation.resume(AuthenticationResult.Error(message))
            }
        }
    }
}
