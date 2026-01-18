/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
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
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.forgot_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.skip
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.passcode.theme.forgotButtonStyle
import org.mifos.authenticator.passcode.theme.skipButtonStyle

@Composable
fun PasscodeSkipButton(
    modifier: Modifier = Modifier,
    onSkipButton: () -> Unit,
    hasPassCode: Boolean,
    textStyle: TextStyle = skipButtonStyle(),
) {
    if (!hasPassCode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = { onSkipButton.invoke() },
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
    textStyle: TextStyle = forgotButtonStyle(),
) {
    if (hasPassCode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(
                onClick = { onForgotButton.invoke() },
            ) {
                Text(
                    text = stringResource(Res.string.forgot_passcode),
                    style = textStyle,
                )
            }
        }
    }
}
