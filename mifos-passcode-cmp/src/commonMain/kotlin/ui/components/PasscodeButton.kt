package com.mifos.passcode.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mifos.passcode.auth.AuthOptions
import com.mifos.passcode.biometric.domain.AuthenticatorStatus
import com.mifos.passcode.ui.theme.forgotButtonStyle
import com.mifos.passcode.ui.theme.skipButtonStyle
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.forgot_passcode
import io.github.openmf.mifos_passcode_cmp.generated.resources.skip
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasscodeSkipButton(
    onSkipButton: () -> Unit,
    hasPassCode: Boolean
) {
    if (!hasPassCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onSkipButton.invoke() }
            ) {
                Text(text = stringResource(Res.string.skip), style = skipButtonStyle())
            }
        }
    }

}

@Composable
fun PasscodeForgotButton(
    onForgotButton: () -> Unit,
    hasPassCode: Boolean
) {
    if (hasPassCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { onForgotButton.invoke() }
            ) {
                Text(
                    text = stringResource(Res.string.forgot_passcode),
                    style = forgotButtonStyle()
                )
            }
        }
    }
}

@Composable
fun SystemAuthenticatorButton(
    onClick: () -> Unit,
    authOptions: List<AuthOptions> = listOf(AuthOptions.UserCredential),
    authenticatorStatus: AuthenticatorStatus
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onClick
        ) {
            if(
                (authOptions.contains(AuthOptions.FaceId) ||
                authOptions.contains(AuthOptions.Fingerprint) ||
                authOptions.contains(AuthOptions.Iris)) && authenticatorStatus.biometricsSet
            ){
                Text("Use Biometrics")
            } else if (authenticatorStatus.userCredentialSet){
                Text("Use Password")
            } else {
                Text("Setup Device Authentication")
            }
        }
    }

}