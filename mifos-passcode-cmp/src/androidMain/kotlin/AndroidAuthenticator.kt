import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import com.mifos.passcode.deviceAuth.domain.AuthenticationResult
import com.mifos.passcode.biometric.domain.AuthenticatorStatus
import com.mifos.passcode.deviceAuth.domain.PlatformAuthenticator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class AndroidAuthenticator(
    val activity: Activity,
): PlatformAuthenticator {
    private val bioMetricManager by lazy {
        BiometricManager.from(activity)
    }
    private val fragmentActivity: FragmentActivity get() = activity as FragmentActivity
    private val authenticatorStatus by  mutableStateOf(AuthenticatorStatus())

    override fun getDeviceAuthenticatorStatus(): AuthenticatorStatus {

        val result = bioMetricManager.canAuthenticate(BIOMETRIC_STRONG)


        val keyguardManager: KeyguardManager =
            activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        authenticatorStatus.userCredentialSet = keyguardManager.isDeviceSecure

        when(result){

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                authenticatorStatus.message = "Hardware unavailable. Try again later."
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                authenticatorStatus.message ="Biometrics not enrolled."
                authenticatorStatus.biometricsNotPossible = false
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                authenticatorStatus.message = "Biometrics not available."
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                authenticatorStatus.message = "Vulnerabilities found. Security update required."
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                authenticatorStatus.message = "Android version not supported."
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                authenticatorStatus.message = "Unable to determine whether the user can authenticate."
            }

            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticatorStatus.biometricsSet = true
                authenticatorStatus.biometricsNotPossible = false
                authenticatorStatus.message = "Biometrics are set."
            }
        }

        return authenticatorStatus

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun setDeviceAuthOption() {
        val enrollBiometric = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            )
        }
        activity.startActivity(enrollBiometric)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun authenticate(): AuthenticationResult = suspendCancellableCoroutine{ continuation ->

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock App")
            .setSubtitle("Unlock using your PIN, Password, Pattern, Face or Fingerprint")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

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