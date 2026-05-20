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
import org.mifos.authenticator.passcode.PasscodeStrings
import org.mifos.authenticator.passcode.defaultPasscodeStrings
import org.mifos.authenticator.passcode.theme.forgotButtonStyle

@Composable
fun PasscodeForgotButton(
    onForgotButton: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = forgotButtonStyle(),
    strings: PasscodeStrings = defaultPasscodeStrings(),
) {
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
                text = strings.forgotPasscode,
                style = textStyle,
            )
        }
    }
}
