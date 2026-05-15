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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.mifos.authenticator.passcode.PasscodeStrings
import org.mifos.authenticator.passcode.defaultPasscodeStrings
import org.mifos.authenticator.passcode.theme.blueTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemAuthSetupConfirmDialog(
    cancelSetup: () -> Unit,
    setSystemAuthentication: () -> Unit,
    containerColor: Color = White,
    shape: Shape = RoundedCornerShape(16.dp),
    titleTextStyle: TextStyle = TextStyle(fontSize = 20.sp),
    descriptionTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    buttonColor: Color = blueTint,
    buttonTextColor: Color = White,
    buttonShape: Shape = ButtonDefaults.shape,
    strings: PasscodeStrings = defaultPasscodeStrings(),
) {
    val dialogProperties = DialogProperties()

    Dialog(
        onDismissRequest = { cancelSetup.invoke() },
        properties = dialogProperties,
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .padding(16.dp),
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = strings.enableExternalAuthDialogTitle,
                    modifier = Modifier
                        .padding(8.dp),
                    style = titleTextStyle,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.enableExternalAuthDialogDescription,
                    modifier = Modifier
                        .padding(8.dp),
                    style = descriptionTextStyle,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    DialogButton(
                        onClick = { cancelSetup.invoke() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f),
                        text = strings.no,
                        containerColor = buttonColor,
                        contentColor = buttonTextColor,
                        shape = buttonShape,
                    )

                    DialogButton(
                        onClick = { setSystemAuthentication.invoke() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                        text = strings.yes,
                        containerColor = buttonColor,
                        contentColor = buttonTextColor,
                        shape = buttonShape,
                    )
                }
            }
        }
    }
}

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = blueTint,
    contentColor: Color = White,
    shape: Shape = ButtonDefaults.shape,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = White,
        ),
    ) {
        Text(text = text)
    }
}
