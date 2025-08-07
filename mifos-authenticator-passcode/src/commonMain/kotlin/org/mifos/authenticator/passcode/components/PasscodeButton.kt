package org.mifos.authenticator.passcode.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.passcode.theme.defaultForgotButtonStyle
import org.mifos.authenticator.passcode.theme.defaultSkipButtonStyle
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.forgot_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.skip
import org.jetbrains.compose.resources.stringResource


data class PasscodeSkipButtonConfig(
    val contentPadding: PaddingValues,
    val border: BorderStroke?,
    val textButtonColor: ButtonColors,
    val content: (@Composable () -> Unit)?,
)

@Composable
fun passcodeSkipButtonConfig(
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    border: BorderStroke? = null,
    textButtonColor: ButtonColors = ButtonDefaults.textButtonColors(),
    content: (@Composable () -> Unit)? = null,
) = PasscodeSkipButtonConfig(
    contentPadding,
    border,
    textButtonColor,
    content,
)

@Composable
fun PasscodeSkipButton(
    hasPassCode: Boolean,
    modifier: Modifier = Modifier,
    config: PasscodeSkipButtonConfig,
    onSkipButton: () -> Unit,
) {
    AnimatedVisibility(!hasPassCode){
        TextButton(
            modifier = modifier,
            onClick = { onSkipButton.invoke() },
            contentPadding = config.contentPadding,
            border = config.border,
            colors = config.textButtonColor
        ) {
            config.content?:
            Text(text = stringResource(Res.string.skip), style = defaultSkipButtonStyle())
        }
    }
}


data class PasscodeForgotButtonConfig(
    val contentPadding: PaddingValues,
    val border: BorderStroke?,
    val textButtonColor: ButtonColors,
    val content: (@Composable () -> Unit)?,
)

@Composable
fun passcodeForgotButtonConfig(
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    border: BorderStroke? = null,
    textButtonColor: ButtonColors = ButtonDefaults.textButtonColors(),
    content: (@Composable () -> Unit)? = null,
) = PasscodeForgotButtonConfig(
    contentPadding,
    border,
    textButtonColor,
    content,
)

@Composable
fun PasscodeForgotButton(
    onForgotButton: () -> Unit,
    hasPassCode: Boolean,
    config: PasscodeForgotButtonConfig,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(hasPassCode){
        TextButton(
            modifier = modifier,
            onClick = { onForgotButton.invoke() },
            contentPadding = config.contentPadding,
            border = config.border,
            colors = config.textButtonColor
        ) {
            config.content?:
            Text(text = stringResource(Res.string.forgot_passcode), style = defaultForgotButtonStyle())
        }
    }
}