/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.passcode

import androidx.compose.runtime.Composable
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_cd_delete_passcode_key
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_cd_toggle_passcode_visibility
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_confirm_old_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_confirm_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_create_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_0
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_1
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_2
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_3
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_4
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_5
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_6
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_7
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_8
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_digit_9
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_enter_your_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_forgot_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_passcode_do_not_match
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_passcode_length_4_digits
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_passcode_length_6_digits
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_skip
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.mifos_passcode_try_again
import org.jetbrains.compose.resources.stringResource

/**
 * Consumer-supplied UI labels for the passcode library.
 *
 * Pass an instance to [org.mifos.authenticator.passcode.screen.PasscodeScreen] (and the
 * lower-level public composables) so the library doesn't bundle translations of its
 * own. Construct it from your app's own resources — typically with `stringResource`
 * calls inside a `@Composable` context — then pass it down.
 *
 * The canonical key set + locale variants live at `l10n-templates/passcode/` in this
 * repo; copy them into your module's `composeResources/values-XX/strings.xml` (keys are
 * prefixed `mifos_passcode_*` to namespace away from your own keys), then build a
 * `PasscodeStrings` from them. See `cmp-sample-shared`'s
 * `PasscodeScreenWithBiometrics.kt#rememberPasscodeStringsFromResources()` for a
 * working reference implementation.
 *
 * @property digits Glyphs for the keypad's 0..9 buttons, indexed by ASCII digit value.
 *   Override only if your locale uses a non-Latin numeral system (e.g. Devanagari ०–९,
 *   Eastern Arabic-Indic ٠–٩, Tamil ௦–௯). Storage and click-callback semantics continue
 *   to use ASCII digits regardless of which glyph is shown.
 */
data class PasscodeStrings(
    val createPasscode: String,
    val confirmPasscode: String,
    val confirmOldPasscode: String,
    val enterPasscode: String,
    val passcodeDoNotMatch: String,
    val tryAgain: String,
    val skip: String,
    val forgotPasscode: String,
    val cdTogglePasscodeVisibility: String,
    val cdDeletePasscodeKey: String,
    val passcodeLength4Digits: String,
    val passcodeLength6Digits: String,
    val digits: List<String>,
) {
    init {
        require(digits.size == 10) {
            "PasscodeStrings.digits must have exactly 10 entries (one per ASCII digit 0..9), got ${digits.size}"
        }
    }
}

/**
 * Locale-aware default [PasscodeStrings] backed by the library's bundled l10n
 * templates (`l10n-templates/passcode/`). Used as the implicit value when a
 * caller doesn't pass an explicit `strings` parameter to a public composable —
 * the device locale picks the appropriate `values-XX/strings.xml`. Override
 * by constructing your own [PasscodeStrings] to draw from any source.
 */
@Composable
fun defaultPasscodeStrings(): PasscodeStrings = PasscodeStrings(
    createPasscode = stringResource(Res.string.mifos_passcode_create_passcode),
    confirmPasscode = stringResource(Res.string.mifos_passcode_confirm_passcode),
    confirmOldPasscode = stringResource(Res.string.mifos_passcode_confirm_old_passcode),
    enterPasscode = stringResource(Res.string.mifos_passcode_enter_your_passcode),
    passcodeDoNotMatch = stringResource(Res.string.mifos_passcode_passcode_do_not_match),
    tryAgain = stringResource(Res.string.mifos_passcode_try_again),
    skip = stringResource(Res.string.mifos_passcode_skip),
    forgotPasscode = stringResource(Res.string.mifos_passcode_forgot_passcode),
    cdTogglePasscodeVisibility = stringResource(Res.string.mifos_passcode_cd_toggle_passcode_visibility),
    cdDeletePasscodeKey = stringResource(Res.string.mifos_passcode_cd_delete_passcode_key),
    passcodeLength4Digits = stringResource(Res.string.mifos_passcode_passcode_length_4_digits),
    passcodeLength6Digits = stringResource(Res.string.mifos_passcode_passcode_length_6_digits),
    digits = listOf(
        stringResource(Res.string.mifos_passcode_digit_0),
        stringResource(Res.string.mifos_passcode_digit_1),
        stringResource(Res.string.mifos_passcode_digit_2),
        stringResource(Res.string.mifos_passcode_digit_3),
        stringResource(Res.string.mifos_passcode_digit_4),
        stringResource(Res.string.mifos_passcode_digit_5),
        stringResource(Res.string.mifos_passcode_digit_6),
        stringResource(Res.string.mifos_passcode_digit_7),
        stringResource(Res.string.mifos_passcode_digit_8),
        stringResource(Res.string.mifos_passcode_digit_9),
    ),
)
