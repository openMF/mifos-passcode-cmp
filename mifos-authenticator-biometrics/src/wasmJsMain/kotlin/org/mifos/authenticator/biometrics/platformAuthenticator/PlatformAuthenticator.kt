/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

actual class PlatformAuthenticator private actual constructor() {

    actual constructor(activity: Any?) : this()
    actual fun getDeviceAuthenticatorStatus(): Set<PlatformAuthenticatorStatus> {
        return setOf(PlatformAuthenticatorStatus.NOT_AVAILABLE)
    }

    actual fun setDeviceAuthOption() {}

    actual suspend fun registerUser(
        userName: String,
        emailId: String,
        displayName: String,
    ): RegistrationResult {
        return RegistrationResult.PlatformAuthenticatorNotAvailable
    }

    actual suspend fun authenticate(title: String, savedRegistrationOutput: String?): AuthenticationResult {
        return AuthenticationResult.UserNotRegistered
    }
}
