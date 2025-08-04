package org.mifos.authenticator.passcode.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.passcode_do_not_match
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.try_again
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.passcode.theme.blueTint


data class PasscodeMismatchedDialogConfig(
    val containerColor: Color = blueTint,
    val shape: Shape = ShapeDefaults.Large,
    val textColor: Color = Color.Black
)

@Composable
fun passcodeMismatchedDialogConfig(
    containerColor: Color = blueTint,
    shape: Shape = ShapeDefaults.Large,
    textColor: Color = Color.Black
) = PasscodeMismatchedDialogConfig(
    containerColor,
    shape,
    textColor
)

@Composable
fun PasscodeMismatchedDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    config: PasscodeMismatchedDialogConfig = passcodeMismatchedDialogConfig()
) {
    if (visible) {
        AlertDialog(
            shape = config.shape,
            containerColor = config.containerColor,
            title = {
                Text(
                    text = stringResource(Res.string.passcode_do_not_match),
                    color = config.textColor
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(Res.string.try_again),
                        color = config.textColor
                    )
                }
            },
            onDismissRequest = onDismiss
        )
    }
}