package com.mifos.passcode.auth.deviceAuth

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
import auth.deviceAuth.AuthenticationResult
import auth.deviceAuth.RegistrationResult
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

            println("Is device secure: ${keyguardManager.isDeviceSecure}")
            println(keyguardManager.toString())
            if(keyguardManager.isDeviceSecure) {
                authenticatorStatus.clear()
                authenticatorStatus.add(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)
            }
        } catch (e: Exception) {
            authenticatorStatus.clear()
            authenticatorStatus.add(PlatformAuthenticatorStatus.NOT_AVAILABLE)
            e.printStackTrace()
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

        println(authenticatorStatus)
        return authenticatorStatus
    }

    @RequiresApi(Build.VERSION_CODES.R)
    actual fun setDeviceAuthOption() {
        val enrollBiometric = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            )
        }
        this.applicationContext?.startActivity(enrollBiometric)
    }

    actual suspend fun authenticate(
        title: String,
        savedRegistrationOutput: String?
    ): AuthenticationResult = suspendCancellableCoroutine { continuation ->

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Unlock using your PIN, Password, Pattern, Face or Fingerprint")
            .setAllowedAuthenticators(
                if (apiLevel > 29) BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                else BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            )
            .build()

        applicationContext?.let { fragmentActivity ->
            val prompt = BiometricPrompt(
                fragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        continuation.resume(AuthenticationResult.Error("${errorCode}: $errString"))
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        AuthenticationResult.Error(message = "Authentication Failed.")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume(AuthenticationResult.Success)
                    }
                }
            )

            prompt.authenticate(promptInfo)
        }
    }

    /**
    Currently this function returns empty string for success result and uses the logic of [authenticate] function.
    In future this function will return an string which a user will need to store and pass it as the value for
    [savedRegistrationOutput].
     */
    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
    ): RegistrationResult {
        val result = authenticate(
            "Register yourself",
            ""
        )
        return when(result){
            is AuthenticationResult.Error -> {
                RegistrationResult.Error("Unknown error")
            }
            is AuthenticationResult.Success -> {
                RegistrationResult.Success("")
            }
            is AuthenticationResult.UserNotRegistered -> {
                RegistrationResult.PlatformAuthenticatorNotSet
            }
        }
    }
}