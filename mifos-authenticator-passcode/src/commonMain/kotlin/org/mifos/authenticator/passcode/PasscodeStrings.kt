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
