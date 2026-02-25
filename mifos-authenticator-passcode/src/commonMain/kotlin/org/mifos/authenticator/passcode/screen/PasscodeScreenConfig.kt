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
 * Configuration for the overall appearance and styling of the `PasscodeScreen`.
 *
 * @property backgroundColor The background color of the entire passcode screen.
 * @property headerTextStyle The [TextStyle] for the header text displayed on the screen.
 */
data class PasscodeAppearanceConfig(
    val backgroundColor: Color = Color.White,
    val headerTextStyle: TextStyle = TextStyle(fontSize = 20.sp),
)

/**
 * Configuration for the logo displayed on the `PasscodeScreen`.
 *
 * @property logoSize The size (width and height) of the logo.
 * @property logoPainter The [Painter] for the logo image. If `null`, a default Mifos logo will be used.
 */
data class PasscodeLogoConfig(
    val logoSize: Dp = 180.dp,
    val logoPainter: Painter? = null,
)

/**
 * Configuration for the visual appearance of the passcode input dots or visible characters.
 *
 * @property dotColor The color of the filled (active) passcode dots.
 * @property inactiveDotColor The color of the unfilled (inactive) passcode dots.
 * @property dotSize The size (diameter) of each passcode dot.
 * @property dotSpacing The horizontal spacing between each passcode dot.
 * @property visiblePasscodeTextStyle The [TextStyle] applied when passcode characters are visible instead of dots.
 */
data class PasscodeDotConfig(
    val dotColor: Color = blueTint,
    val inactiveDotColor: Color = Color.Gray,
    val dotSize: Dp = 14.dp,
    val dotSpacing: Dp = 26.dp,
    val visiblePasscodeTextStyle: TextStyle = TextStyle(color = blueTint, fontSize = 24.sp),
)

/**
 * Configuration for the numeric and action keys displayed on the `PasscodeScreen` keypad.
 *
 * @property shouldShuffleKeys If `true`, the numeric keys will be shuffled for security.
 * @property keyTextStyle The [TextStyle] for the text displayed on each key. If `null`, a default style will be used.
 * @property keyColor The content color of the keys (e.g., text color).
 * @property keyShape The [Shape] of the key buttons.
 * @property keyElevation The [CardElevation] for the key buttons. If `null`, a default elevation will be used.
 * @property keyContainerColor The background color of the key buttons.
 * @property keySize The size (width and height) of each key button.
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
 * Configuration for the "Skip" and "Forgot Passcode" buttons on the `PasscodeScreen` keypad.
 *
 * @property skipButtonTextStyle The [TextStyle] for the "Skip" button. If `null`, a default style will be used.
 * @property forgotButtonTextStyle The [TextStyle] for the "Forgot Passcode" button. If `null`, a default style will be used.
 */
data class PasscodeButtonConfig(
    val skipButtonTextStyle: TextStyle? = null,
    val forgotButtonTextStyle: TextStyle? = null,
)

/**
 * Configuration for the passcode length switch (e.g., 4-digit vs. 6-digit).
 *
 * @property switchTabColor The color of the active tab in the switch.
 * @property switchEnabledColor The background color of the enabled (selected) switch option.
 * @property switchEnabledTextColor The text color for the enabled (selected) switch option.
 * @property switchDisabledTextColor The text color for the disabled (unselected) switch option.
 * @property switchTextStyle The [TextStyle] for the text within the switch options. If `null`, a default style will be used.
 */
data class PasscodeSwitchConfig(
    val switchTabColor: Color = blueTint,
    val switchEnabledColor: Color = Color.LightGray.copy(alpha = .7f),
    val switchEnabledTextColor: Color = Color.Black,
    val switchDisabledTextColor: Color = Color.White,
    val switchTextStyle: TextStyle? = null,
)

/**
 * Configuration for the "Passcode Mismatched" dialog that appears on error.
 *
 * @property dialogContainerColor The background color of the dialog.
 * @property dialogTitleColor The color of the dialog's title text.
 * @property dialogButtonTextColor The text color for the buttons within the dialog.
 * @property dialogShape The [Shape] of the dialog. If `null`, a default MaterialTheme shape will be used.
 */
data class PasscodeDialogConfig(
    val dialogContainerColor: Color = Color.White,
    val dialogTitleColor: Color = Color.Black,
    val dialogButtonTextColor: Color = Color.Black,
    val dialogShape: Shape? = null,
)
