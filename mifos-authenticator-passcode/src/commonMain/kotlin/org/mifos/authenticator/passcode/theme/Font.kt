/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.passcode.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Lato_Black
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Lato_Bold
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Lato_Regular
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun LatoFonts() = FontFamily(
    Font(
        resource = Res.font.Lato_Bold,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    ),
    Font(
        resource = Res.font.Lato_Regular,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
    ),
    Font(
        resource = Res.font.Lato_Black,
        weight = FontWeight.Black,
        style = FontStyle.Normal,
    ),
)
