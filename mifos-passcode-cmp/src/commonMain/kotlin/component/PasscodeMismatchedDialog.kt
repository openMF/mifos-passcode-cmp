package com.mifos.passcode.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mifos.mifos_passcode_cmp.generated.resources.Res
import com.mifos.mifos_passcode_cmp.generated.resources.passcode_do_not_match
import com.mifos.mifos_passcode_cmp.generated.resources.try_again
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasscodeMismatchedDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            shape = MaterialTheme.shapes.large,
            containerColor = Color.White,
            title = {
                Text(
                        text = stringResource(Res.string.passcode_do_not_match),
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.try_again), color = Color.Black)
                }
            },
            onDismissRequest = onDismiss
        )
    }
}