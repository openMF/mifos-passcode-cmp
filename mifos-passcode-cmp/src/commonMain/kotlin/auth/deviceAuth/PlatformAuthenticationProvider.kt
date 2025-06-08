package com.mifos.passcode.auth.deviceAuth

import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages platform-specific user authentication (e.g., Biometrics, Windows Hello).
 *
 * This class handles user registration and authentication flows, providing reactive state
 * for the current availability and status of platform authenticators. It is designed to be
 * lifecycle-aware via the [updateAuthenticatorStatus] method.
 *
 * @param activity provided current activity in Android. It can be null for other platforms.
 */



final class PlatformAuthenticationProvider(val activity: Any? = null){

    private val authenticator = PlatformAuthenticator(activity)

    private val mutex = Mutex()

    private val _authenticatorStatus = MutableStateFlow(deviceAuthenticatorStatus())

    // Check the support for platform authenticator according to the platform
    fun deviceAuthenticatorStatus() = authenticator.getDeviceAuthenticatorStatus()


    private fun updateAuthenticatorStatus() {
        _authenticatorStatus.value = deviceAuthenticatorStatus()
    }

    suspend fun registerUser(): RegistrationResult {
        mutex.withLock {
            updateAuthenticatorStatus()
            val notAvailable = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE)
            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)

            if(notAvailable){
                return RegistrationResult.PlatformAuthenticatorNotAvailable
            }else if(notSet){
                return RegistrationResult.PlatformAuthenticatorNotSet
            }

            try {
                return authenticator.registerUser()
            } catch (e: Exception) {
                return RegistrationResult.Error("Registration failed: ${e.message}")
            }
        }
    }

    suspend fun onAuthenticatorClick(appName: String= "", savedRegistrationData: String?=null): AuthenticationResult {
        mutex.withLock {
            updateAuthenticatorStatus()

            val notSet = _authenticatorStatus.value.contains(PlatformAuthenticatorStatus.NOT_SETUP)
            if(notSet){ return AuthenticationResult.UserNotRegistered }

            try {
                println("Saved data: $savedRegistrationData")
                return authenticator.authenticate(appName, savedRegistrationData)
            } catch (e: Exception) {
                return AuthenticationResult.Error("Authentication failed: ${e.message}")
            }
        }
    }

    fun setupPlatformAuthenticator(){
        authenticator.setDeviceAuthOption()
    }

}



