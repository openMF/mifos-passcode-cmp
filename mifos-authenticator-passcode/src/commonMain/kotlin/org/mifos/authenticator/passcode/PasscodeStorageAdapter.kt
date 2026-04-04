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
 * An interface for adapting persistent storage mechanisms for passcodes.
 *
 * Implementations of this interface are responsible for securely saving, loading,
 * and deleting passcodes. This allows the `PasscodeManager` to be platform-agnostic
 * regarding data storage.
 */
interface PasscodeStorageAdapter {
    /**
     * Saves the given passcode to persistent storage.
     *
     * @param passcode The passcode string to be saved. It is recommended that this
     *                  passcode is encrypted or hashed before storage.
     */
    fun savePasscode(passcode: String)

    /**
     * Loads the stored passcode from persistent storage.
     *
     * @return The loaded passcode string, or `null` if no passcode is stored.
     */
    fun loadPasscode(): String?

    /**
     * Deletes the currently stored passcode from persistent storage.
     */
    fun deletePasscode()

    /**
yes     * Saves external authentication registration data (e.g. FIDO/WebAuthn credential) to persistent storage.
     *
     * @param registrationData The registration data string to be saved.
     */
    @Deprecated(
        message = "Use BiometricStorageAdapter from the biometrics library instead.",
        replaceWith = ReplaceWith(
            "BiometricStorageAdapter.saveRegistrationData(registrationData)",
            "org.mifos.authenticator.biometrics.BiometricStorageAdapter",
        ),
    )
    fun saveRegistrationData(registrationData: String)

    /**
     * Loads the stored external authentication registration data from persistent storage.
     *
     * @return The loaded registration data, or `null` if none is stored.
     */
    @Deprecated(
        message = "Use BiometricStorageAdapter from the biometrics library instead.",
        replaceWith = ReplaceWith(
            "BiometricStorageAdapter.loadRegistrationData()",
            "org.mifos.authenticator.biometrics.BiometricStorageAdapter",
        ),
    )
    fun loadRegistrationData(): String?

    /**
     * Deletes the external authentication registration data from persistent storage.
     */
    @Deprecated(
        message = "Use BiometricStorageAdapter from the biometrics library instead.",
        replaceWith = ReplaceWith(
            "BiometricStorageAdapter.deleteRegistrationData()",
            "org.mifos.authenticator.biometrics.BiometricStorageAdapter",
        ),
    )
    fun deleteRegistrationData()
}
