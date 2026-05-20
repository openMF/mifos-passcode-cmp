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
 * Represents the result of a platform authentication operation.
 *
 * This sealed interface encapsulates the possible outcomes of an authentication attempt,
 * which can be a success, an error, or an indication that the user is not registered.
 */
sealed interface AuthenticationResult {
    /**
     * Indicates that the authentication was successful.
     */
    data object Success : AuthenticationResult

    /**
     * Indicates that an error occurred during authentication.
     *
     * The consumer should map [error] to a user-facing string from its own localized
     * resources; the library no longer surfaces platform prose directly because it is
     * always in the device locale rather than the app locale.
     *
     * @property error The categorized error reason.
     */
    data class Error(val error: BiometricError) : AuthenticationResult

    /**
     * Indicates that user cancelled the authentication request.
     */
    data object UserCancelled : AuthenticationResult

    /**
     * Indicates that the authentication failed because the user is not registered.
     */
    data object UserNotRegistered : AuthenticationResult
}
