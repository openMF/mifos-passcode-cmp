package com.mifos.passcode.auth.passcode.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mifos.passcode.ui.theme.blueTint
import com.mifos.passcode.ui.theme.changePasscodeLengthStyle
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
fun PasscodeLengthChangeButton(
    onPasscodeLengthChange: (Int) -> Unit,
    hasPassCode: Boolean,
    switchState: Boolean
) {
    if (!hasPassCode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                switchState,
                onCheckedChange = {
                    when(it){
                        true -> onPasscodeLengthChange(6)
                        false -> onPasscodeLengthChange(4)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = blueTint,
                    uncheckedThumbColor = blueTint,
                    uncheckedBorderColor = blueTint,
                    uncheckedTrackColor = Color.White
                )
            )

            Spacer(Modifier.width(6.dp))

            Text("Change passcode length")
        }

    }
}