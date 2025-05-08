package com.mifos.passcode.auth.passcode.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mifos.passcode.Platform
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.deviceAuth.presentation.AuthenticatorStatus
import com.mifos.passcode.ui.theme.forgotButtonStyle
import com.mifos.passcode.ui.theme.skipButtonStyle
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.face_scan
import io.github.openmf.mifos_passcode_cmp.generated.resources.fingerprint
import io.github.openmf.mifos_passcode_cmp.generated.resources.forgot_passcode
import io.github.openmf.mifos_passcode_cmp.generated.resources.keypad
import io.github.openmf.mifos_passcode_cmp.generated.resources.skip
import org.jetbrains.compose.resources.painterResource
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
    platformAuthOptions: List<PlatformAuthOptions> = listOf(PlatformAuthOptions.UserCredential),
    authenticatorStatus: AuthenticatorStatus,
    platform: Platform
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
                (platformAuthOptions.contains(PlatformAuthOptions.FaceId) ||
                platformAuthOptions.contains(PlatformAuthOptions.Fingerprint) ||
                platformAuthOptions.contains(PlatformAuthOptions.Iris)) && authenticatorStatus.biometricsSet
            ){
                if(platform.name.lowercase()=="ios"){
                    Image(
                        painter = painterResource(Res.drawable.face_scan),
                        contentDescription = "Use Biometrics",
                        modifier = Modifier.size(50.dp)
                    )
                } else if(platform.name.lowercase()=="android"){
                    Image(
                        painter = painterResource(Res.drawable.fingerprint),
                        contentDescription = "Use Biometrics",
                        modifier = Modifier.size(50.dp)
                    )
                } else Text("Use Biometrics")

            } else if (authenticatorStatus.userCredentialSet){
                Image(
                    painter = painterResource(Res.drawable.keypad),
                    contentDescription = "Use Password",
                    modifier = Modifier.size(50.dp)
                )
            } else {
                Text("Setup Device Authentication")
            }
        }
    }

}