/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics

/**
 * An interface for persisting biometric registration data (e.g. FIDO/WebAuthn credentials).
 *
 * Implementations should store data securely using platform-appropriate mechanisms
 * (e.g. EncryptedSharedPreferences on Android, Keychain on iOS).
 */
interface BiometricStorageAdapter {
    /**
     * Saves biometric registration data to persistent storage.
     *
     * @param registrationData The registration data string to be saved.
     */
    fun saveRegistrationData(registrationData: String)

    /**
     * Loads the stored biometric registration data from persistent storage.
     *
     * @return The loaded registration data, or `null` if none is stored.
     */
    fun loadRegistrationData(): String?

    /**
     * Deletes the biometric registration data from persistent storage.
     */
    fun deleteRegistrationData()
}
