package com.example.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.mifos.passcode.BiometricUtilAndroidImpl
import com.mifos.passcode.CipherUtilAndroidImpl
import com.mifos.passcode.PasscodeRepository
import com.mifos.passcode.PasscodeRepositoryImpl
import com.mifos.passcode.component.PasscodeScreen
import com.mifos.passcode.utility.PreferenceManager

class MainActivity : FragmentActivity() {

    private val bioMetricUtil by lazy {
        BiometricUtilAndroidImpl(this, CipherUtilAndroidImpl())
    }
    private lateinit var passcodeRepository: PasscodeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bioMetricUtil.preparePrompt(
            title = "Biometric Authentication",
            subtitle = "",
            description = "Use your fingerprint to log in"
        )

        passcodeRepository = PasscodeRepositoryImpl(PreferenceManager())

        setContent {
            MyApp()
        }
    }

    @Composable
    fun MyApp() {
        var isLogin by remember { mutableStateOf(false) }

        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (isLogin) {
                    LoginText()
                } else {
                    PasscodeScreen(
                        onForgotButton = { onPasscodeForgot() },
                        onSkipButton = { onPasscodeSkip() },
                        onPasscodeConfirm = { passcode -> onPassCodeReceive(passcode, onLoginSuccess = { isLogin = true }) },
                        onPasscodeRejected = { onPasscodeReject() },
                        enableBiometric = true,
                        bioMetricUtil = bioMetricUtil,
                        onBiometricAuthSuccess = { isLogin = true },
                    )
                }
            }
        }
    }

    private fun onPassCodeReceive(passcode: String, onLoginSuccess: () -> Unit) {
        if (passcodeRepository.getSavedPasscode() == passcode) {
            onLoginSuccess()
        }
    }

    private fun onPasscodeReject() {}

    private fun onPasscodeForgot() {
        // Redirect to login page or reset passcode
    }

    private fun onPasscodeSkip() {
        finish()
    }

    @Composable
    fun LoginText() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "You are logged in!", style = MaterialTheme.typography.headlineMedium)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MyApp()
    }
}