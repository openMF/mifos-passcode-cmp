/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
@file:Suppress("PropertyName")

package org.mifos.authenticator.biometrics.platformAuthenticator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages platform-specific user authentication (e.g., Biometrics, Windows Hello, Face ID).
 *
 * This class provides a high-level interface for user registration and authentication flows,
 * exposing the current availability and status of platform authenticators as a reactive state.
 * It is designed to be lifecycle-aware, and its status should be updated via the
 * [updateAuthenticatorStatus] method.
 *
 * @param activity A platform-specific activity or context. For Android, this should be a
 * `FragmentActivity`. For other platforms, it can be `null`.
 */
class PlatformAuthenticationProvider(activity: Any? = null) {
    private val authenticator = PlatformAuthenticator(activity)

    private val mutex = Mutex()

    // A MutableStateFlow to hold and observe the current status of the device authenticator.
    // It's initialized with the current status obtained from the authenticator.
    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())
    /**
     * A [StateFlow] that emits the current status of the device's platform authenticator.
     *
     * This flow can be observed to reactively update the UI based on the availability and
     * configuration of authentication methods like biometrics or device credentials.
     */
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    /**
     * This private function checks the support and current status of the platform authenticator on the device.
     * This function directly delegates to the underlying [org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticator].
     *
     * @return A set of [PlatformAuthenticatorStatus] indicating the current state of the device authenticator.
     */
    private fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()

    /**
     * Updates the [authenticatorStatus] with the latest status of the device authenticator.
     *
     * This function should be called before performing registration or authentication to ensure
     * that the status is up-to-date, especially when the app resumes or when platform
     * settings may have changed.
     */
    fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    /**
     * Initiates the user registration process using the platform authenticator.
     *
     * Before attempting registration, it checks the current authenticator status to ensure that
     * a platform authenticator is available and configured. This function is thread-safe.
     *
     * @param userName A unique identifier for the user. If left empty, a random Base64-encoded
     * ID will be generated.
     * @param emailId The user's email address. If left empty, a dummy email ID will be used.
     * @param displayName The display name for the user. If left empty, a default display name
     * will be used.
     *
     * @return A [RegistrationResult] indicating the outcome of the registration attempt:
     * - [RegistrationResult.PlatformAuthenticatorNotAvailable]: If biometrics are not available.
     * - [RegistrationResult.PlatformAuthenticatorNotSet]: If the authenticator is not set up.
     * - [RegistrationResult.Success]: If the registration is successful. This may contain
     *   registration data that needs to be stored.
     * - [RegistrationResult.Error]: If an unexpected error occurs during registration.
     */
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult {
        mutex.withLock {
            updateAuthenticatorStatus()
            val notAvailable = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)

            if (notAvailable) {
                return RegistrationResult.PlatformAuthenticatorNotAvailable
            } else if (notSet) {
                return RegistrationResult.PlatformAuthenticatorNotSet
            }

            return try {
                authenticator.registerUser(
                    userName,
                    emailId,
                    displayName,
                )
            } catch (e: Exception) {
                RegistrationResult.Error("Registration failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Initiates the authentication process using the platform authenticator.
     *
     * Before attempting authentication, it checks if the authenticator is set up. This function
     * is thread-safe.
     *
     * @param appName An optional name of the application requesting authentication.
     * @param savedRegistrationData An optional string containing previously saved registration data,
     * which may be required for certain authentication flows (e.g., on Windows).
     *
     * @return An [AuthenticationResult] indicating the outcome of the authentication attempt:
     * - [AuthenticationResult.UserNotRegistered]: If the authenticator is not set up.
     * - [AuthenticationResult.Success]: If the authentication is successful.
     * - [AuthenticationResult.Error]: If an unexpected error occurs during authentication.
     */
    suspend fun onAuthenticatorClick(
        appName: String = "",
        savedRegistrationData: String? = null,
    ): AuthenticationResult {
        mutex.withLock {
            updateAuthenticatorStatus()

            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)
            if (notSet) {
                return AuthenticationResult.UserNotRegistered
            }

            return try {
                authenticator.authenticate(appName, savedRegistrationData)
            } catch (e: Exception) {
                AuthenticationResult.Error("Authentication failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Prompts the user to set up the platform authenticator (e.g., the device's screen lock)
     * if it has not been configured yet.
     *
     * This function delegates directly to the underlying [PlatformAuthenticator].
     */
    fun setupPlatformAuthenticator() {
        authenticator.setDeviceAuthOption()
    }
}
