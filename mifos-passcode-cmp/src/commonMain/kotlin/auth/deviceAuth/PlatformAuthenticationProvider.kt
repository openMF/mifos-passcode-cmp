package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages platform-specific user authentication (e.g., Biometrics, Windows Hello).
 *
 * This class handles user registration and authentication flows, providing reactive state
 * for the current availability and status of platform authenticators. It is designed to be
 * lifecycle-aware via the [updateAuthenticatorStatus] method.
 *
 * @param activity provides FragmentActivity for Android implementation. By default, it is null for all
 * platform and has no effect.
 */

class PlatformAuthenticationProvider(activity: Any? = null) {
    private val authenticator = PlatformAuthenticator(activity)

    private val mutex = Mutex()

    // A MutableStateFlow to hold and observe the current status of the device authenticator.
    // It's initialized with the current status obtained from the authenticator.
    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    /**
     * This private function checks the support and current status of the platform authenticator on the device.
     * This function directly delegates to the underlying [PlatformAuthenticator].
     *
     * @return A set of [PlatformAuthenticatorStatus] indicating the current state of the device authenticator.
     */
    private fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()

    /**
     * Updates the [authenticatorStatus] [MutableStateFlow] state with the latest status
     * of the device authenticator. This function should be called before performing
     * registration or authentication to ensure the status is up-to-date.
     */
    fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    /**
     * Initiates the user registration process using the platform authenticator.
     * Before attempting registration, it checks the current authenticator status.
     *
     * This function is thread-safe due to the use of a [Mutex].
     *
     * @param userName takes the unique userId of the user. If left empty a random Base64Encoded userId will be
     * generated and used instead.
     * @param emailId takes the user email Id. If left empty a dummy email id will be used "mifos@mifos.com".
     * @param displayName take the display name for the user. If left empty a default display name "Mifos" will
     * be used instead.
     * @return A [RegistrationResult] indicating the outcome of the registration attempt:
     * - [RegistrationResult.PlatformAuthenticatorNotAvailable] if biometrics are not available.
     * - [RegistrationResult.PlatformAuthenticatorNotSet] if the authenticator is not set up.
     * - [RegistrationResult.Success] The actual result from [PlatformAuthenticator.registerUser] on success.
     *  This class also contains the registration that has to be saved.
     * - [RegistrationResult.Error] if an unexpected exception occurs during registration.
     *  This class also holds the type of error received as is only @param
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
                    displayName
                )
            } catch (e: Exception) {
                RegistrationResult.Error("Registration failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Initiates the authentication process using the platform authenticator.
     * Before attempting authentication, it checks if the authenticator is set up.
     *
     * This function is thread-safe due to the use of a [Mutex].
     *
     * @param appName An optional name of the application requesting authentication. Defaults to an empty string.
     * @param savedRegistrationData An optional string containing previously saved registration data,
     * which might be required for certain authentication flows. Defaults to null.
     * @return An [AuthenticationResult] indicating the outcome of the authentication attempt:
     * - [AuthenticationResult.UserNotRegistered] if the authenticator is not set up.
     * - [AuthenticationResult.Success] The actual result from [PlatformAuthenticator.authenticate] on success.
     * - [AuthenticationResult.Error] if an unexpected exception occurs during authentication.
     * This class also holds the type of error received as is only @param
     */
    suspend fun onAuthenticatorClick(appName: String = "", savedRegistrationData: String? = null)
    : AuthenticationResult {
        mutex.withLock {
            updateAuthenticatorStatus()

            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)
            if (notSet) { return AuthenticationResult.UserNotRegistered }

            return try {
                authenticator.authenticate(appName, savedRegistrationData)
            } catch (e: Exception) {
                AuthenticationResult.Error("Authentication failed: ${e.message}")
            }
        }
    }

    /**
     * Prompts the user to set up the platform authenticator, the screen lock of they device,
     * in case the user has not set up their platform authenticator (device lock) for authentication
     * already.
     * This function delegates directly to the underlying [PlatformAuthenticator].
     */
    fun setupPlatformAuthenticator() {
        authenticator.setDeviceAuthOption()
    }
}
