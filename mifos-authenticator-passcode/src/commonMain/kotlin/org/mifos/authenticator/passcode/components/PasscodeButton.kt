package org.mifos.authenticator.passcode.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.core.designsystem.theme.forgotButtonStyle
import org.mifos.authenticator.core.designsystem.theme.skipButtonStyle
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.forgot_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.skip
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasscodeSkipButton(
    modifier: Modifier = Modifier,
    onSkipButton: () -> Unit,
    hasPassCode: Boolean,
    textStyle: TextStyle = skipButtonStyle()
) {
    if (!hasPassCode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onSkipButton.invoke() }
            ) {
                Text(text = stringResource(Res.string.skip), style = textStyle)
            }
        }
    }
}

@Composable
fun PasscodeForgotButton(
    modifier: Modifier = Modifier,
    onForgotButton: () -> Unit,
    hasPassCode: Boolean,
    textStyle: TextStyle = forgotButtonStyle()
) {
    if (hasPassCode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { onForgotButton.invoke() }
            ) {
                Text(
                    text = stringResource(Res.string.forgot_passcode),
                    style = textStyle
                )
            }
        }
    }
}