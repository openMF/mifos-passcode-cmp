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
 * Defines the types of platform authentication methods available on a device.
 *
 * This enum is used to represent the specific authentication options that a user can
 * choose from, such as Face ID, fingerprint, or device credentials.
 */
enum class PlatformAuthOptions {
    /**
     * Face recognition authentication (e.g., Apple's Face ID).
     */
    FaceId,

    /**
     * Fingerprint authentication.
     */
    Fingerprint,

    /**
     * Iris scanning authentication.
     */
    Iris,

    /**
     * Voice recognition authentication.
     */
    Voice,

    /**
     * Device credentials, such as a PIN, password, or pattern.
     */
    UserCredential,
}
