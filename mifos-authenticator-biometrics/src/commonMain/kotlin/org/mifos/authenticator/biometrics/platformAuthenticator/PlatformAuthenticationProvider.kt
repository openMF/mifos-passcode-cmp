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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mifos.authenticator.biometrics.BiometricStorageAdapter

/**
 * Manages platform-specific user authentication (e.g., Biometrics, Windows Hello, Face ID).
 *
 * This class provides a high-level interface for user registration and authentication flows,
 * exposing the current availability and status of platform authenticators as a reactive state.
 *
 * Registration data persistence is handled internally via the supplied
 * [BiometricStorageAdapter]: [registerUser] saves on success, [onAuthenticatorClick] loads
 * on demand, and [unregister] deletes. Consumers do not need to call the adapter directly.
 */
class PlatformAuthenticationProvider(
    private val authenticator: PlatformAuthenticator,
    private val biometricStorageAdapter: BiometricStorageAdapter,
) {

    private val mutex = Mutex()

    private val _authenticatorStatus = MutableStateFlow(authenticator.getDeviceAuthenticatorStatus())

    /**
     * A [StateFlow] that emits the current status of the device's platform authenticator.
     */
    val authenticatorStatus: StateFlow<Set<PlatformAuthenticatorStatus>> = _authenticatorStatus.asStateFlow()

    private val _isRegistered = MutableStateFlow(biometricStorageAdapter.loadRegistrationData() != null)

    /**
     * A [StateFlow] that reflects whether a registration blob is currently persisted.
     * `true` once [registerUser] succeeds; `false` after [unregister] or when
     * [onAuthenticatorClick] detects an invalidated registration.
     */
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = authenticator.getDeviceAuthenticatorStatus()
    }

    /**
     * Initiates the user registration process and persists the resulting registration blob
     * via the configured [BiometricStorageAdapter] on success.
     *
     * Prompt strings ([title], [subtitle], [description], [negativeButtonText]) are
     * forwarded to the platform's prompt UI on platforms that consume them. The library
     * does not bundle translations for prompt text; the consumer should pass already-localized
     * strings sourced from its own resources.
     */
    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
        title: String = "",
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "",
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
                val result = authenticator.registerUser(
                    userName = userName,
                    emailId = emailId,
                    displayName = displayName,
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    negativeButtonText = negativeButtonText,
                )
                if (result is RegistrationResult.Success) {
                    biometricStorageAdapter.saveRegistrationData(result.message)
                    _isRegistered.value = true
                }
                result
            } catch (e: Exception) {
                RegistrationResult.Error(BiometricError.Unknown(platformMessage = e.message))
            }
        }
    }

    /**
     * Authenticates the user via the platform authenticator, loading any previously saved
     * registration data internally.
     *
     * Prompt strings ([title], [subtitle], [description], [negativeButtonText]) follow
     * the same locale convention as [registerUser] — consumer-supplied, already localized.
     *
     * If the platform reports [AuthenticationResult.UserNotRegistered], the stored blob is
     * invalid and is deleted automatically; [isRegistered] becomes `false`.
     */
    suspend fun onAuthenticatorClick(
        title: String = "",
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "",
    ): AuthenticationResult {
        mutex.withLock {
            updateAuthenticatorStatus()

            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)
            if (notSet) {
                return AuthenticationResult.UserNotRegistered
            }

            val savedRegistrationData = biometricStorageAdapter.loadRegistrationData()
            if (savedRegistrationData == null) {
                _isRegistered.value = false
                return AuthenticationResult.UserNotRegistered
            }

            return try {
                val result = authenticator.authenticate(
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    negativeButtonText = negativeButtonText,
                    savedRegistrationOutput = savedRegistrationData,
                )
                if (result is AuthenticationResult.UserNotRegistered) {
                    biometricStorageAdapter.deleteRegistrationData()
                    _isRegistered.value = false
                }
                result
            } catch (e: Exception) {
                AuthenticationResult.Error(BiometricError.Unknown(platformMessage = e.message))
            }
        }
    }

    /**
     * Deletes the persisted registration blob and flips [isRegistered] to `false`.
     * Call this on user-initiated "disable biometrics" / logout flows.
     */
    suspend fun unregister() {
        mutex.withLock {
            biometricStorageAdapter.deleteRegistrationData()
            _isRegistered.value = false
        }
    }

    /**
     * Prompts the user to set up the platform authenticator (e.g., the device's screen lock).
     */
    fun setupPlatformAuthenticator() {
        authenticator.setDeviceAuthOption()
    }
}
