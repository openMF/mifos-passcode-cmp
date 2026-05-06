/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.platformAuthentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import mifos_authenticator.cmp_sample_shared.generated.resources.Res
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_prompt_subtitle
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_prompt_title
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_cd_delete_passcode_key
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_cd_toggle_passcode_visibility
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_confirm_old_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_confirm_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_create_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_0
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_1
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_2
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_3
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_4
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_5
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_6
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_7
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_8
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_digit_9
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_enable_external_auth_dialog_description
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_enable_external_auth_dialog_title
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_enter_your_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_forgot_passcode
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_no
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_passcode_do_not_match
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_passcode_length_4_digits
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_passcode_length_6_digits
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_skip
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_try_again
import mifos_authenticator.cmp_sample_shared.generated.resources.mifos_passcode_yes
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.PasscodeStrings
import org.mifos.authenticator.passcode.screen.PasscodeScreen

/**
 * Wraps [PasscodeScreen] with biometric-button integration.
 *
 * Bridges the biometrics and passcode libraries, which never import each other.
 * Surfaces two independent callbacks:
 *  - [onPasscodeResult] for passcode-flow events (Verified, Created, Changed, Forgotten, Rejected)
 *  - [onBiometricSuccess] for a biometric authentication succeeding
 *
 * Biometric success is **not** translated into a [PasscodeResult] — it is delivered via
 * its own callback so neither library leaks the other's concepts. The consumer decides
 * whether the two paths converge to the same destination.
 *
 * Set [hideBiometricButton] to `true` for flows where biometric bypass would defeat the
 * security check (e.g. "verify passcode to disable biometrics"). When `true`, the button
 * is suppressed even if a biometric registration exists.
 *
 * Must be hosted inside a `PlatformAuthenticatorCompositionProvider` so the
 * `platformAuthenticationProvider` CompositionLocal is available.
 */
@Composable
fun PasscodeScreenWithBiometrics(
    passcodeManager: PasscodeManager,
    onPasscodeResult: (PasscodeResult) -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricError: (String) -> Unit = {},
    biometricPromptTitle: String = stringResource(Res.string.biometric_prompt_title),
    biometricPromptSubtitle: String = stringResource(Res.string.biometric_prompt_subtitle),
    biometricPromptDescription: String = "",
    biometricPromptNegativeButtonText: String = "",
    hideBiometricButton: Boolean = false,
    passcodeStrings: PasscodeStrings = rememberPasscodeStringsFromResources(),
) {
    val authProvider = platformAuthenticationProvider.current
    val isRegistered by authProvider.isRegistered.collectAsState()

    PasscodeScreen(
        passcodeManager = passcodeManager,
        onResult = onPasscodeResult,
        isExternalAuthEnabled = isRegistered && !hideBiometricButton,
        externalAuthButton = { modifier ->
            BiometricKey(
                modifier = modifier,
                title = biometricPromptTitle,
                subtitle = biometricPromptSubtitle,
                description = biometricPromptDescription,
                negativeButtonText = biometricPromptNegativeButtonText,
                onSuccess = onBiometricSuccess,
                onUserNotRegistered = { /* no-op; isRegistered flips via provider */ },
                onAuthenticationError = onBiometricError,
            )
        },
        strings = passcodeStrings,
    )
}

/**
 * Resolves a [PasscodeStrings] instance from this sample app's own `composeResources`,
 * using keys prefixed `mifos_passcode_*` (populated from the published `l10n-templates/`
 * once the library stops bundling its own translations).
 *
 * Memoized via [remember] keyed on the underlying string values so the data class is
 * reconstructed only when an underlying resource changes (e.g. locale switch).
 */
@Composable
private fun rememberPasscodeStringsFromResources(): PasscodeStrings {
    val createPasscode = stringResource(Res.string.mifos_passcode_create_passcode)
    val confirmPasscode = stringResource(Res.string.mifos_passcode_confirm_passcode)
    val confirmOldPasscode = stringResource(Res.string.mifos_passcode_confirm_old_passcode)
    val enterPasscode = stringResource(Res.string.mifos_passcode_enter_your_passcode)
    val passcodeDoNotMatch = stringResource(Res.string.mifos_passcode_passcode_do_not_match)
    val tryAgain = stringResource(Res.string.mifos_passcode_try_again)
    val skip = stringResource(Res.string.mifos_passcode_skip)
    val forgotPasscode = stringResource(Res.string.mifos_passcode_forgot_passcode)
    val enableExternalAuthDialogTitle = stringResource(Res.string.mifos_passcode_enable_external_auth_dialog_title)
    val enableExternalAuthDialogDescription = stringResource(Res.string.mifos_passcode_enable_external_auth_dialog_description)
    val yes = stringResource(Res.string.mifos_passcode_yes)
    val no = stringResource(Res.string.mifos_passcode_no)
    val cdTogglePasscodeVisibility = stringResource(Res.string.mifos_passcode_cd_toggle_passcode_visibility)
    val cdDeletePasscodeKey = stringResource(Res.string.mifos_passcode_cd_delete_passcode_key)
    val passcodeLength4Digits = stringResource(Res.string.mifos_passcode_passcode_length_4_digits)
    val passcodeLength6Digits = stringResource(Res.string.mifos_passcode_passcode_length_6_digits)
    val digit0 = stringResource(Res.string.mifos_passcode_digit_0)
    val digit1 = stringResource(Res.string.mifos_passcode_digit_1)
    val digit2 = stringResource(Res.string.mifos_passcode_digit_2)
    val digit3 = stringResource(Res.string.mifos_passcode_digit_3)
    val digit4 = stringResource(Res.string.mifos_passcode_digit_4)
    val digit5 = stringResource(Res.string.mifos_passcode_digit_5)
    val digit6 = stringResource(Res.string.mifos_passcode_digit_6)
    val digit7 = stringResource(Res.string.mifos_passcode_digit_7)
    val digit8 = stringResource(Res.string.mifos_passcode_digit_8)
    val digit9 = stringResource(Res.string.mifos_passcode_digit_9)
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
