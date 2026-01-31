/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
@file:Suppress("EmptyKtFile")

package org.mifos.authenticator.passcode.components

// TODO: Fix passcode toolbar for new implementation of passcode logic and use it.

//
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.AlertDialog
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.Shape
// import androidx.compose.ui.text.TextStyle
// import androidx.compose.ui.unit.Dp
// import androidx.compose.ui.unit.dp
// import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
// import mifos_authenticator.mifos_authenticator_passcode.generated.resources.are_you_sure_you_want_to_exit
// import mifos_authenticator.mifos_authenticator_passcode.generated.resources.cancel
// import mifos_authenticator.mifos_authenticator_passcode.generated.resources.exit
// import org.jetbrains.compose.resources.stringResource
// import org.mifos.authenticator.passcode.theme.blueTint
//
// @Composable
// fun PasscodeToolbar(
//    modifier: Modifier = Modifier,
//    hasPasscode: Boolean,
//    indicatorActiveColor: Color = blueTint,
//    indicatorInactiveColor: Color = Color.Gray,
//    indicatorWidth: Dp = 80.dp,
//    indicatorHeight: Dp = 4.dp,
//    indicatorSpacing: Dp = 20.dp,
//    indicatorShape: Shape = MaterialTheme.shapes.medium,
// ) {
//    var exitWarningDialogVisible by remember { mutableStateOf(false) }
//    ExitWarningDialog(
//        visible = exitWarningDialogVisible,
//        onConfirm = {},
//        onDismiss = {
//            exitWarningDialogVisible = false
//        },
//    )
//
// //    Row(
// //        modifier = modifier
// //            .fillMaxWidth()
// //            .padding(top = 8.dp),
// //        horizontalArrangement = Arrangement.Center,
// //    ) {
// //        if (!hasPasscode) {
// //            PasscodeStepIndicator(
// //                activeStep = activeStep,
// //                activeColor = indicatorActiveColor,
// //                inactiveColor = indicatorInactiveColor,
// //                indicatorWidth = indicatorWidth,
// //                indicatorHeight = indicatorHeight,
// //                spacing = indicatorSpacing,
// //                shape = indicatorShape,
// //            )
// //        }
// //    }
// }
//
// @Composable
// fun ExitWarningDialog(
//    visible: Boolean,
//    onConfirm: () -> Unit,
//    onDismiss: () -> Unit,
//    containerColor: Color = Color.White,
//    titleColor: Color = Color.Black,
//    shape: Shape = MaterialTheme.shapes.large,
//    titleTextStyle: TextStyle = TextStyle.Default,
//    buttonTextStyle: TextStyle = TextStyle.Default,
// ) {
//    if (visible) {
//        AlertDialog(
//            shape = shape,
//            containerColor = containerColor,
//            title = {
//                Text(
//                    text = stringResource(Res.string.are_you_sure_you_want_to_exit),
//                    color = titleColor,
//                    style = titleTextStyle,
//                )
//            },
//            confirmButton = {
//                TextButton(onClick = onConfirm) {
//                    Text(text = stringResource(Res.string.exit), style = buttonTextStyle)
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = onDismiss) {
//                    Text(text = stringResource(Res.string.cancel), style = buttonTextStyle)
//                }
//            },
//            onDismissRequest = onDismiss,
//        )
//    }
// }
