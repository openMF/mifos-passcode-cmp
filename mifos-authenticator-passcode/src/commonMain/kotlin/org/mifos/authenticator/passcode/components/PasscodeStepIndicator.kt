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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.passcode.theme.blueTint
import org.mifos.authenticator.passcode.utility.Constants
import org.mifos.authenticator.passcode.utility.Step

@Composable
fun PasscodeStepIndicator(
    modifier: Modifier = Modifier,
    activeStep: Step,
    activeColor: Color = blueTint,
    inactiveColor: Color = Color.Gray,
    indicatorWidth: Dp = 80.dp,
    indicatorHeight: Dp = 4.dp,
    spacing: Dp = 20.dp,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        repeat(Constants.STEPS_COUNT) { step ->
            val isActiveStep = step <= activeStep.index
            val stepColor =
                animateColorAsState(if (isActiveStep) activeColor else inactiveColor, label = "")

            Box(
                modifier = Modifier
                    .size(
                        width = indicatorWidth,
                        height = indicatorHeight,
                    )
                    .background(
                        color = stepColor.value,
                        shape = shape,
                    ),
            )
        }
    }
}
