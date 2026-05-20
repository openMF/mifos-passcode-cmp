/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

/**
 * Categorical, locale-independent representation of a biometric authentication or
 * registration failure.
 *
 * Consumers should map each case to a user-facing string from their own localized
 * resources rather than display platform messages directly. The platform's prose
 * ([Unknown.platformMessage]) is preserved only as a debugging hint and is in the
 * device locale, not the app locale.
 */
sealed interface BiometricError {

    /** Too many failed attempts; biometric temporarily disabled by the platform. */
    data object Lockout : BiometricError

    /** Lockout that requires another auth factor to clear (Android `ERROR_LOCKOUT_PERMANENT`). */
    data object LockoutPermanent : BiometricError

    /** Sensor present but currently unavailable (driver fault, security update needed, etc.). */
    data object HardwareUnavailable : BiometricError

    /** No biometric is enrolled on the device. */
    data object NotEnrolled : BiometricError

    /** Authentication did not complete within the platform's time limit. */
    data object Timeout : BiometricError

    /** Insufficient storage to complete the operation (Android `ERROR_NO_SPACE`). */
    data object NoSpace : BiometricError

    /**
     * The stored registration payload supplied to authenticate() could not be parsed.
     * Currently only emitted by the desktop (Windows Hello) actual; the consumer typically
     * re-runs registration to recover.
     */
    data object InvalidRegistrationData : BiometricError

    /**
     * Native platform reported invalid arguments while performing [stage]. Currently only
     * emitted by the desktop (Windows Hello) actual.
     */
    data class InvalidArguments(val stage: AuthStage) : BiometricError

    /**
     * Any platform error not covered by the categorized cases.
     *
     * @property code Platform-specific error code where available
     *   (Android `BiometricPrompt.ERROR_*`, iOS `LAError`).
     * @property platformMessage Platform-supplied message in the device locale, for logs only.
     */
    data class Unknown(
        val code: Int? = null,
        val platformMessage: String? = null,
    ) : BiometricError
}

/** Which platform-authenticator phase a [BiometricError] originated from. */
enum class AuthStage { Authentication, Registration }
