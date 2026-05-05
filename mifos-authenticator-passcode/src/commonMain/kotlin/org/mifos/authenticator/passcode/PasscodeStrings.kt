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
import androidx.compose.runtime.remember
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.Res
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.cd_delete_passcode_key
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.cd_toggle_passcode_visibility
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.confirm_old_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.confirm_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.create_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_0
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_1
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_2
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_3
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_4
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_5
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_6
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_7
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_8
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.digit_9
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.enable_external_auth_dialog_description
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.enable_external_auth_dialog_title
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.enter_your_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.forgot_passcode
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.no
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.passcode_do_not_match
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.passcode_length_4_digits
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.passcode_length_6_digits
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.skip
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.try_again
import mifos_authenticator.mifos_authenticator_passcode.generated.resources.yes
import org.jetbrains.compose.resources.stringResource

/**
 * Consumer-supplied UI labels for the passcode library.
 *
 * Pass an instance to [org.mifos.authenticator.passcode.screen.PasscodeScreen] (and the
 * lower-level public composables) so the library doesn't need to bundle translations of
 * its own. Construct it from your app's own resources — typically with [stringResource]
 * calls inside a `@Composable` context — then pass it down.
 *
 * If you don't supply one, [defaultPasscodeStrings] resolves the bundled English
 * defaults from the library's own resources (backward-compatible behavior).
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
    val enableExternalAuthDialogTitle: String,
    val enableExternalAuthDialogDescription: String,
    val yes: String,
    val no: String,
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
 * Resolves [PasscodeStrings] from the library's bundled default resources. Used as the
 * default value for the `strings` parameter on the public composables so existing
 * consumers keep working without supplying their own copy.
 *
 * The body is wrapped in [remember] keyed on the underlying string values, so the
 * `PasscodeStrings` instance is reconstructed only when an underlying resource changes
 * (e.g. locale switch). Subsequent recompositions return the same instance — preventing
 * a per-frame allocation for every leaf composable that uses this default.
 *
 * Prefer constructing your own [PasscodeStrings] from your app's resources — that's the
 * intended customization path.
 */
@Composable
fun defaultPasscodeStrings(): PasscodeStrings {
    val createPasscode = stringResource(Res.string.create_passcode)
    val confirmPasscode = stringResource(Res.string.confirm_passcode)
    val confirmOldPasscode = stringResource(Res.string.confirm_old_passcode)
    val enterPasscode = stringResource(Res.string.enter_your_passcode)
    val passcodeDoNotMatch = stringResource(Res.string.passcode_do_not_match)
    val tryAgain = stringResource(Res.string.try_again)
    val skip = stringResource(Res.string.skip)
    val forgotPasscode = stringResource(Res.string.forgot_passcode)
    val enableExternalAuthDialogTitle = stringResource(Res.string.enable_external_auth_dialog_title)
    val enableExternalAuthDialogDescription = stringResource(Res.string.enable_external_auth_dialog_description)
    val yes = stringResource(Res.string.yes)
    val no = stringResource(Res.string.no)
    val cdTogglePasscodeVisibility = stringResource(Res.string.cd_toggle_passcode_visibility)
    val cdDeletePasscodeKey = stringResource(Res.string.cd_delete_passcode_key)
    val passcodeLength4Digits = stringResource(Res.string.passcode_length_4_digits)
    val passcodeLength6Digits = stringResource(Res.string.passcode_length_6_digits)
    val digit0 = stringResource(Res.string.digit_0)
    val digit1 = stringResource(Res.string.digit_1)
    val digit2 = stringResource(Res.string.digit_2)
    val digit3 = stringResource(Res.string.digit_3)
    val digit4 = stringResource(Res.string.digit_4)
    val digit5 = stringResource(Res.string.digit_5)
    val digit6 = stringResource(Res.string.digit_6)
    val digit7 = stringResource(Res.string.digit_7)
    val digit8 = stringResource(Res.string.digit_8)
    val digit9 = stringResource(Res.string.digit_9)
    return remember(
        createPasscode, confirmPasscode, confirmOldPasscode, enterPasscode,
        passcodeDoNotMatch, tryAgain, skip, forgotPasscode,
        enableExternalAuthDialogTitle, enableExternalAuthDialogDescription,
        yes, no, cdTogglePasscodeVisibility, cdDeletePasscodeKey,
        passcodeLength4Digits, passcodeLength6Digits,
        digit0, digit1, digit2, digit3, digit4, digit5, digit6, digit7, digit8, digit9,
    ) {
        PasscodeStrings(
            createPasscode = createPasscode,
            confirmPasscode = confirmPasscode,
            confirmOldPasscode = confirmOldPasscode,
            enterPasscode = enterPasscode,
            passcodeDoNotMatch = passcodeDoNotMatch,
            tryAgain = tryAgain,
            skip = skip,
            forgotPasscode = forgotPasscode,
            enableExternalAuthDialogTitle = enableExternalAuthDialogTitle,
            enableExternalAuthDialogDescription = enableExternalAuthDialogDescription,
            yes = yes,
            no = no,
            cdTogglePasscodeVisibility = cdTogglePasscodeVisibility,
            cdDeletePasscodeKey = cdDeletePasscodeKey,
            passcodeLength4Digits = passcodeLength4Digits,
            passcodeLength6Digits = passcodeLength6Digits,
            digits = listOf(digit0, digit1, digit2, digit3, digit4, digit5, digit6, digit7, digit8, digit9),
        )
    }
}
