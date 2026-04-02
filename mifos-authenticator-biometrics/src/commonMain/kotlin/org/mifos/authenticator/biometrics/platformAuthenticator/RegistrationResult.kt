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
 * Represents the result of a user registration operation with a platform authenticator.
 *
 * This sealed interface encapsulates the possible outcomes of a registration attempt,
 * which can be a success, an error, or an indication that the platform authenticator is not
 * available or not set up.
 */
sealed interface RegistrationResult {
    /**
     * Indicates that the registration was successful.
     *
     * @property message A descriptive message confirming the successful registration.
     */
    data class Success(val message: String) : RegistrationResult

    /**
     * Indicates that an error occurred during registration.
     *
     * @property message A descriptive message explaining the error.
     */
    data class Error(val message: String) : RegistrationResult

    data object UserCancelled : RegistrationResult

    /**
     * Indicates that the registration failed because the platform authenticator is not set up.
     */
    data object PlatformAuthenticatorNotSet : RegistrationResult

    /**
     * Indicates that the registration failed because the platform authenticator is not available.
     */
    data object PlatformAuthenticatorNotAvailable : RegistrationResult
}
