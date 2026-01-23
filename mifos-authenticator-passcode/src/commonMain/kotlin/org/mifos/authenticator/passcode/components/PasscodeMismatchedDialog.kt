/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.passcode.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.passcode_do_not_match
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.try_again
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasscodeMismatchedDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    containerColor: Color = Color.White,
    titleColor: Color = Color.Black,
    buttonTextColor: Color = Color.Black,
    shape: Shape = MaterialTheme.shapes.large,
    titleTextStyle: TextStyle = TextStyle.Default,
    buttonTextStyle: TextStyle = TextStyle.Default,
) {
    if (visible) {
        AlertDialog(
            shape = shape,
            containerColor = containerColor,
            title = {
                Text(
                    text = stringResource(Res.string.passcode_do_not_match),
                    color = titleColor,
                    style = titleTextStyle,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(Res.string.try_again),
                        color = buttonTextColor,
                        style = buttonTextStyle,
                    )
                }
            },
            onDismissRequest = onDismiss,
        )
    }
}
