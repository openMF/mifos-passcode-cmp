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
import org.mifos.authenticator.passcode.PasscodeStrings

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
    strings: PasscodeStrings,
) {
    if (visible) {
        AlertDialog(
            shape = shape,
            containerColor = containerColor,
            title = {
                Text(
                    text = strings.passcodeDoNotMatch,
                    color = titleColor,
                    style = titleTextStyle,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = strings.tryAgain,
                        color = buttonTextColor,
                        style = buttonTextStyle,
                    )
                }
            },
            onDismissRequest = onDismiss,
        )
    }
}
