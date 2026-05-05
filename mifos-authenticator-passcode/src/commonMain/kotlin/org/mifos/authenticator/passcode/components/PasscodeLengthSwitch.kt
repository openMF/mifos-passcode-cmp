/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
@file:Suppress("PropertyName")

package org.mifos.authenticator.passcode.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mifos.authenticator.passcode.PasscodeStrings
import org.mifos.authenticator.passcode.theme.blueTint
import org.mifos.authenticator.passcode.theme.changePasscodeLengthStyle
import org.mifos.authenticator.passcode.utility.PasscodeLength

@Composable
fun PasscodeLengthSwitch(
    modifier: Modifier = Modifier,
    passcodeLength: PasscodeLength = PasscodeLength.FOUR_DIGIT,
    tabColor: Color = blueTint,
    enabledSwitchColor: Color = Color.LightGray.copy(alpha = .7f),
    enabledTextColor: Color = Color.Black,
    // Text color on the sliding tab
    disabledTextColor: Color = Color.White,
    textStyle: TextStyle = changePasscodeLengthStyle(),
    minWidth: Dp = 150.dp,
    height: Dp = 35.dp,
    shape: Shape = RoundedCornerShape(40.dp),
    onSelectFourDigit: () -> Unit = {},
    onSelectSixDigit: () -> Unit = {},
    strings: PasscodeStrings,
) {
    var selectedPasscodeLength by remember {
        mutableStateOf(passcodeLength)
    }

    val fourDigitsLabel = strings.passcodeLength4Digits
    val sixDigitsLabel = strings.passcodeLength6Digits

    // Sizes to max(minWidth, intrinsic width of widest label-button pair) so
    // long-label locales (e.g. ta "4 இலக்கங்கள்") don't clip while short-label
    // locales preserve the compact 150.dp baseline. Row.intrinsicMaxWidth =
    // sum of children's intrinsics, and Box.IntrinsicSize.Max takes the max
    // across children — so the Box ends up as wide as Row demands, which is
    // what we want.
    Box(
        modifier = modifier
            .height(height)
            .widthIn(min = minWidth)
            .width(IntrinsicSize.Max)
            .clip(shape)
            .background(enabledSwitchColor),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Button(
                {
                    selectedPasscodeLength = PasscodeLength.FOUR_DIGIT
                    onSelectFourDigit()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = enabledTextColor,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                enabled = selectedPasscodeLength == PasscodeLength.SIX_DIGIT,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            ) {
                Text(fourDigitsLabel, style = textStyle.copy(color = enabledTextColor))
            }

            Button(
                {
                    selectedPasscodeLength = PasscodeLength.SIX_DIGIT
                    onSelectSixDigit()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = enabledTextColor,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                enabled = selectedPasscodeLength == PasscodeLength.FOUR_DIGIT,
            ) {
                Text(sixDigitsLabel, style = textStyle.copy(color = enabledTextColor))
            }
        }

        AnimatedContent(
            targetState = selectedPasscodeLength,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clip(shape),
            transitionSpec = {
                val isGoingRight = targetState.length > initialState.length
                slideInHorizontally(
                    spring(
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialOffsetX = {
                        if (isGoingRight) it else -it
                    },
                ) togetherWith slideOutHorizontally(
                    spring(
                        stiffness = Spring.StiffnessLow,
                    ),
                    targetOffsetX = {
                        if (isGoingRight) -it else it
                    },
                )
            },
            content = { currentPasscodeLength ->
                SlidingTab(
                    label =
                    if (currentPasscodeLength == PasscodeLength.SIX_DIGIT) {
                        sixDigitsLabel
                    } else {
                        fourDigitsLabel
                    },
                    alignment =
                    if (currentPasscodeLength == PasscodeLength.SIX_DIGIT) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    },
                    color = tabColor,
                    labelColor = disabledTextColor,
                    textStyle = textStyle,
                    shape = shape,
                )
            },

            label = "",
        )
    }
}

@Composable
private fun SlidingTab(
    label: String,
    color: Color,
    labelColor: Color,
    alignment: Alignment,
    textStyle: TextStyle,
    shape: Shape,
) {
    Box(
        contentAlignment = alignment,
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(.5f)
                .clip(shape)
                .background(color),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(label, style = textStyle.copy(color = labelColor))
        }
    }
}
