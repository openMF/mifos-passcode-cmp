/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.passcode.screen

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardElevation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mifos.authenticator.passcode.theme.blueTint

/**
 * Configuration for the passcode screen appearance and styling
 */
data class PasscodeAppearanceConfig(
    val backgroundColor: Color = Color.White,
    val headerTextStyle: TextStyle = TextStyle(fontSize = 20.sp),
)

/**
 * Configuration for logo/branding
 */
data class PasscodeLogoConfig(
    val logoSize: Dp = 180.dp,
    val logoPainter: Painter? = null,
)

/**
 * Configuration for passcode dots/indicators
 */
data class PasscodeDotConfig(
    val dotColor: Color = blueTint,
    val inactiveDotColor: Color = Color.Gray,
    val dotSize: Dp = 14.dp,
    val dotSpacing: Dp = 26.dp,
    val visiblePasscodeTextStyle: TextStyle = TextStyle(color = blueTint, fontSize = 24.sp),
)

/**
 * Configuration for passcode key buttons
 */
data class PasscodeKeyConfig(
    val shouldShuffleKeys: Boolean = true,
    val keyTextStyle: TextStyle? = null,
    val keyColor: Color = blueTint,
    val keyShape: Shape = CircleShape,
    val keyElevation: CardElevation? = null,
    val keyContainerColor: Color = Color.White,
    val keySize: Dp = 60.dp,
)

/**
 * Configuration for action buttons (skip and forgot)
 */
data class PasscodeButtonConfig(
    val skipButtonTextStyle: TextStyle? = null,
    val forgotButtonTextStyle: TextStyle? = null,
)

/**
 * Configuration for the passcode length switch
 */
data class PasscodeSwitchConfig(
    val switchTabColor: Color = blueTint,
    val switchEnabledColor: Color = Color.LightGray.copy(alpha = .7f),
    val switchEnabledTextColor: Color = Color.Black,
    val switchDisabledTextColor: Color = Color.White,
    val switchTextStyle: TextStyle? = null,
)

/**
 * Configuration for the toolbar indicators
 */
data class PasscodeToolbarConfig(
    val toolbarIndicatorActiveColor: Color = blueTint,
    val toolbarIndicatorInactiveColor: Color = Color.Gray,
)

/**
 * Configuration for the passcode mismatch dialog
 */
data class PasscodeDialogConfig(
    val dialogContainerColor: Color = Color.White,
    val dialogTitleColor: Color = Color.Black,
    val dialogButtonTextColor: Color = Color.Black,
    val dialogShape: Shape? = null,
)
