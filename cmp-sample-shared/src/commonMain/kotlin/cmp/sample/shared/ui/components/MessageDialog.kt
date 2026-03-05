/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

enum class DialogBoxType {
    None,
    ERROR,
    NOT_SET,
}

@Composable
fun MessageDiaglogBox(
    onDismissRequest: () -> Unit,
    dialogMessage: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Message") },
        text = { Text(text = dialogMessage) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("OK")
            }
        },
    )
}
