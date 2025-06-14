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
 * @param activity provides FragmentActivity for Android implementation. By default, it is null for all platform and has no effect.
 */

class PlatformAuthenticationProvider(activity: Any? = null){

    private val authenticator = PlatformAuthenticator(activity)

    private val mutex = Mutex()

    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())
    val authenticatorStatus = _authenticatorStatus.asStateFlow()

    init {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    // Check the support for platform authenticator according to the platform
    private fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()

    fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    suspend fun registerUser(
        userName: String = "",
        emailId: String = "",
        displayName: String = "",
    ): RegistrationResult {
        mutex.withLock {
            updateAuthenticatorStatus()
            val notAvailable = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)

            if(notAvailable){
                return RegistrationResult.PlatformAuthenticatorNotAvailable
            }else if(notSet){
                return RegistrationResult.PlatformAuthenticatorNotSet
            }

            return try {
                authenticator.registerUser(
                    userName,
                    emailId,
                    displayName
                )
            } catch (e: Exception) {
                RegistrationResult.Error("Registration failed: ${e.message}")
            }
        }
    }

    suspend fun onAuthenticatorClick(appName: String= "", savedRegistrationData: String?=null): AuthenticationResult {
        mutex.withLock {
            updateAuthenticatorStatus()

            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)
            if(notSet){ return AuthenticationResult.UserNotRegistered }

            return try {
                authenticator.authenticate(appName, savedRegistrationData)
            } catch (e: Exception) {
                AuthenticationResult.Error("Authentication failed: ${e.message}")
            }
        }
    }

    fun setupPlatformAuthenticator(){
        authenticator.setDeviceAuthOption()
    }

}


