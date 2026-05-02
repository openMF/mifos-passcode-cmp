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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import mifos_authenticator.cmp_sample_shared.generated.resources.Res
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_hardware_unavailable
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_invalid_arguments_auth
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_invalid_arguments_registration
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_invalid_registration_data
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_lockout
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_no_space
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_not_enrolled
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_timeout
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_error_unknown
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_prompt_subtitle
import mifos_authenticator.cmp_sample_shared.generated.resources.biometric_prompt_title
import org.jetbrains.compose.resources.stringResource
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthStage
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.BiometricError
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthOptions
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.authenticator.biometrics.platformAvailableAuthenticationOption
import org.mifos.authenticator.passcode.components.PasscodeKey

@Composable
fun BiometricKey(
    modifier: Modifier,
    title: String = stringResource(Res.string.biometric_prompt_title),
    subtitle: String = stringResource(Res.string.biometric_prompt_subtitle),
    description: String = "",
    negativeButtonText: String = "",
    onSuccess: () -> Unit,
    onUserNotRegistered: () -> Unit,
    onAuthenticationError: (String) -> Unit,
) {
    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val platformAvailableAuthenticationOption = platformAvailableAuthenticationOption.current
    val platformAuthOptions by platformAvailableAuthenticationOption.currentAuthOption.collectAsState()
    val authenticatorStatus by platformAuthenticationProvider.authenticatorStatus.collectAsState()
    val scope = rememberCoroutineScope()

    val errorMessages = rememberBiometricErrorMessages()

    val isBiometricAvailable = authenticatorStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET) ||
        authenticatorStatus.contains(PlatformAuthenticatorStatus.DEVICE_CREDENTIAL_SET)

    if (isBiometricAvailable) {
        val icon: ImageVector = when {
            platformAuthOptions.contains(PlatformAuthOptions.Fingerprint) -> Icons.Default.Fingerprint
            platformAuthOptions.contains(PlatformAuthOptions.FaceId) -> Icons.Default.Face
            else -> Icons.Default.Lock
        }

        PasscodeKey(
            modifier = modifier,
            keyIcon = icon,
            onClick = {
                scope.launch {
                    val result = platformAuthenticationProvider.onAuthenticatorClick(
                        title = title,
                        subtitle = subtitle,
                        description = description,
                        negativeButtonText = negativeButtonText,
                    )
                    when (result) {
                        is AuthenticationResult.Success -> onSuccess()
                        is AuthenticationResult.Error -> onAuthenticationError(errorMessages.localize(result.error))
                        is AuthenticationResult.UserNotRegistered -> onUserNotRegistered()
                        AuthenticationResult.UserCancelled -> { }
                    }
                }
            },
        )
    }
}

/**
 * Pre-resolved localized strings for each [BiometricError] case, bundled so callers can
 * map errors to strings outside a `@Composable` context (e.g. inside `scope.launch`).
 */
internal class BiometricErrorMessages(
    val lockout: String,
    val hardwareUnavailable: String,
    val notEnrolled: String,
    val timeout: String,
    val noSpace: String,
    val invalidRegistrationData: String,
    val invalidArgumentsAuth: String,
    val invalidArgumentsRegistration: String,
    val unknown: String,
) {
    fun localize(error: BiometricError): String = when (error) {
        BiometricError.Lockout, BiometricError.LockoutPermanent -> lockout
        BiometricError.HardwareUnavailable -> hardwareUnavailable
        BiometricError.NotEnrolled -> notEnrolled
        BiometricError.Timeout -> timeout
        BiometricError.NoSpace -> noSpace
        BiometricError.InvalidRegistrationData -> invalidRegistrationData
        is BiometricError.InvalidArguments -> when (error.stage) {
            AuthStage.Authentication -> invalidArgumentsAuth
            AuthStage.Registration -> invalidArgumentsRegistration
        }
        is BiometricError.Unknown -> unknown
    }
}

@Composable
internal fun rememberBiometricErrorMessages(): BiometricErrorMessages = BiometricErrorMessages(
    lockout = stringResource(Res.string.biometric_error_lockout),
    hardwareUnavailable = stringResource(Res.string.biometric_error_hardware_unavailable),
    notEnrolled = stringResource(Res.string.biometric_error_not_enrolled),
    timeout = stringResource(Res.string.biometric_error_timeout),
    noSpace = stringResource(Res.string.biometric_error_no_space),
    invalidRegistrationData = stringResource(Res.string.biometric_error_invalid_registration_data),
    invalidArgumentsAuth = stringResource(Res.string.biometric_error_invalid_arguments_auth),
    invalidArgumentsRegistration = stringResource(Res.string.biometric_error_invalid_arguments_registration),
    unknown = stringResource(Res.string.biometric_error_unknown),
)
