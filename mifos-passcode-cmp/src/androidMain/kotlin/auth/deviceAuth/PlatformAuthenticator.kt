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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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

    val apiLevel = Build.VERSION.SDK_INT

    private val _authenticatorStatus  = MutableStateFlow(
        PlatformAuthenticatorStatus.MobileAuthenticatorStatus()
    )


    actual fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus  {

        val result = bioMetricManager?.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

        try {
            val keyguardManager: KeyguardManager =
                applicationContext?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            _authenticatorStatus.update {
                it.copy(
                    userCredentialSet = keyguardManager.isDeviceSecure
                )
            }
        }catch (e: Exception){
            e.printStackTrace()
        }finally {
            when(result){
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Hardware unavailable. Try again later."
                        )
                    }
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Biometrics not enrolled.",
                            biometricsNotPossible = false
                        )
                    }
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Biometrics not available."
                        )
                    }
                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Vulnerabilities found. Security update required."
                        )
                    }
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Android version not supported."
                        )
                    }
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Unable to determine whether the user can authenticate."
                        )
                    }
                }

                BiometricManager.BIOMETRIC_SUCCESS -> {
                    _authenticatorStatus.update {
                        it.copy(
                            message = "Biometrics are set.",
                            biometricsSet = true,
                            biometricsNotPossible = false
                        )
                    }
                }
            }
        }

        return _authenticatorStatus.value
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

    actual suspend fun authenticate(title: String, savedRegistrationOutput: String): AuthenticationResult = suspendCancellableCoroutine{ continuation ->

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Unlock using your PIN, Password, Pattern, Face or Fingerprint")
            .setAllowedAuthenticators(
                if(apiLevel>29) BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                else BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            )
            .build()

        applicationContext?.let {fragmentActivity ->
            val prompt = BiometricPrompt(
                fragmentActivity,
                object: BiometricPrompt.AuthenticationCallback(){

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        continuation.resume(AuthenticationResult.Error("${errorCode}: $errString"))
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        AuthenticationResult.Failed()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume(AuthenticationResult.Success())
                    }
                }
            )

            prompt.authenticate(promptInfo)
        }
    }

    actual suspend fun registerUser(): AuthenticationResult {
        return AuthenticationResult.Success("Already setup")
    }

}