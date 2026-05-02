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

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class PlatformAuthenticator private actual constructor() {

    private var applicationContext: FragmentActivity? = null
    private var bioMetricManager: BiometricManager? = null

    actual constructor(activity: Any?) : this() {
        applicationContext = activity as? FragmentActivity
        applicationContext?.let {
            bioMetricManager = BiometricManager.from(it)
        }
    }

    private val apiLevel = Build.VERSION.SDK_INT

    private val authenticatorStatus = mutableSetOf(PlatformAuthenticatorStatus.NOT_SETUP)

    actual fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus> {
        val result = bioMetricManager?.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

        try {
            val keyguardManager: KeyguardManager =
                applicationContext?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (keyguardManager.isDeviceSecure) {
                authenticatorStatus.clear()
                authenticatorStatus.add(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
            }
        } catch (e: Exception) {
            authenticatorStatus.clear()
            authenticatorStatus.add(PlatformAuthenticatorStatus.NOT_AVAILABLE)
            Logger.e { e.stackTraceToString() }
        } finally {
            when (result) {
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_UNAVAILABLE)
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_SET)
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_UNAVAILABLE)
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
                }

                BiometricManager.BIOMETRIC_SUCCESS -> {
                    authenticatorStatus.add(PlatformAuthenticatorStatus.BIOMETRICS_SET)
                }
            }
        }

        Logger.d { "authenticatorStatus=$authenticatorStatus" }
        return authenticatorStatus
    }

    @RequiresApi(Build.VERSION_CODES.R)
    actual fun setDeviceAuthOption() {
        val enrollBiometric = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
            )
        }
        this.applicationContext?.startActivity(enrollBiometric)
    }

    actual suspend fun authenticate(
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
        savedRegistrationOutput: String?,
    ): AuthenticationResult = suspendCancellableCoroutine { continuation ->

        val allowedAuthenticators = if (apiLevel > Build.VERSION_CODES.Q) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (subtitle.isNotEmpty()) setSubtitle(subtitle)
                if (description.isNotEmpty()) setDescription(description)
                // setNegativeButtonText is mutually exclusive with DEVICE_CREDENTIAL.
                // Since DEVICE_CREDENTIAL is always allowed above, the negative button is
                // system-supplied ("Use PIN" / "Use Pattern") and any consumer-set text would
                // throw at build(). We deliberately ignore [negativeButtonText] on Android.
            }
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        applicationContext?.let { fragmentActivity ->
            val prompt = BiometricPrompt(
                fragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        continuation.resume(
                            when (errorCode) {
                                BiometricPrompt.ERROR_CANCELED,
                                BiometricPrompt.ERROR_USER_CANCELED,
                                BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                                -> AuthenticationResult.UserCancelled

                                else -> AuthenticationResult.Error(
                                    mapAndroidBiometricError(errorCode, errString.toString()),
                                )
                            },
                        )
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Logger.w { "Biometric authentication attempt failed, prompt remains open for retry" }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume(AuthenticationResult.Success)
                    }
                },
            )

            prompt.authenticate(promptInfo)
        }
    }

    /**
     *Currently this function returns empty string for success result and uses the logic of `authenticate` function.
     *In future this function will return a string which a user will need to store and pass it as the value for
     *`savedRegistrationOutput`.
     */
    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
    ): RegistrationResult {
        val result = authenticate(
            title = title,
            subtitle = subtitle,
            description = description,
            negativeButtonText = negativeButtonText,
            savedRegistrationOutput = "",
        )
        return when (result) {
            is AuthenticationResult.Error -> RegistrationResult.Error(result.error)
            is AuthenticationResult.Success -> RegistrationResult.Success("")
            is AuthenticationResult.UserNotRegistered -> RegistrationResult.PlatformAuthenticatorNotSet
            is AuthenticationResult.UserCancelled -> RegistrationResult.UserCancelled
        }
    }
}

private fun mapAndroidBiometricError(errorCode: Int, message: String?): BiometricError = when (errorCode) {
    BiometricPrompt.ERROR_LOCKOUT -> BiometricError.Lockout
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricError.LockoutPermanent
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED,
    -> BiometricError.HardwareUnavailable
    BiometricPrompt.ERROR_NO_BIOMETRICS,
    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
    -> BiometricError.NotEnrolled
    BiometricPrompt.ERROR_TIMEOUT -> BiometricError.Timeout
    BiometricPrompt.ERROR_NO_SPACE -> BiometricError.NoSpace
    else -> BiometricError.Unknown(code = errorCode, platformMessage = message)
}
