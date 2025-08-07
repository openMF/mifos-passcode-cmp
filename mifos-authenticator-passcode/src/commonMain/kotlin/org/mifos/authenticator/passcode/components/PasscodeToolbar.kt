package org.mifos.authenticator.passcode.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.are_you_sure_you_want_to_exit
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.cancel
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.exit
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.passcode.utility.Step


@Composable
fun PasscodeToolbar(
    activeStep: Step,
    hasPasscode: Boolean,
    exitWarningDialogConfig: ExitWarningDialogConfig,
    passcodeStepIndicatorConfig: PasscodeStepIndicatorConfig,
    modifier: Modifier = Modifier,
) {
    var exitWarningDialogVisible by remember { mutableStateOf(false) }
    ExitWarningDialog(
        visible = exitWarningDialogVisible, onConfirm = {}, onDismiss = {
        exitWarningDialogVisible = false
    }, config = exitWarningDialogConfig
    )

    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (!hasPasscode) {
            PasscodeStepIndicator(
                activeStep = activeStep, config = passcodeStepIndicatorConfig
            )
        }
    }
}

data class ExitWarningDialogConfig(
    val containerColor: Color,
    val shape: Shape,
    val textStyle: TextStyle,
    val contentPadding: PaddingValues,
    val border: BorderStroke?,
    val textButtonColor: ButtonColors,
    val title: String?,
    val confirmButtonText: String?,
    val dismissButtonContent: String?,
)

@Composable
fun exitWarningDialogConfig(
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textStyle: TextStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface
    ),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    border: BorderStroke? = null,
    textButtonColor: ButtonColors = ButtonDefaults.textButtonColors(),
    title: String? = "",
    confirmButtonText: String? = "",
    dismissButtonContent: String? = "",
) = ExitWarningDialogConfig(
    containerColor,
    shape,
    textStyle,
    contentPadding,
    border,
    textButtonColor,
    title,
    confirmButtonText,
    dismissButtonContent
)

@Composable
fun ExitWarningDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    config: ExitWarningDialogConfig,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            shape = config.shape,
            containerColor = config.containerColor,
            title = {
                Text(
                    text = config.title?: stringResource(Res.string.are_you_sure_you_want_to_exit),
                    style = config.textStyle
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    contentPadding = config.contentPadding,
                    border = config.border,
                    colors = config.textButtonColor,
                ) {
                    Text(
                        text = config.confirmButtonText?: stringResource(Res.string.exit), style = config.textStyle
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = config.contentPadding,
                    border = config.border,
                    colors = config.textButtonColor,
                ) {
                    Text(text = config.dismissButtonContent?: stringResource(Res.string.cancel), style = config.textStyle)
                }
            },
            onDismissRequest = onDismiss
        )
    }
}