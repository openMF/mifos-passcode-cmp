/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.biometrics.windows

import com.sun.jna.Native
import org.mifos.authenticator.biometrics.mockServer.RegistrationDataGET
import org.mifos.authenticator.biometrics.mockServer.RegistrationDataPOST
import org.mifos.authenticator.biometrics.mockServer.VerificationDataGET
import org.mifos.authenticator.biometrics.mockServer.VerificationDataPOST

final class WindowsHelloAuthenticatorNativeSupportImpl : WindowsHelloAuthenticatorNativeSupport {

    private val native by lazy {
        Native.load("WindowsHelloAuthenticator", WindowsHelloAuthenticatorNativeSupport::class.java)
    }

    override fun checkIfAuthenticatorIsAvailable(): Boolean {
        return native.checkIfAuthenticatorIsAvailable()
    }

    override fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue {
        return native.verifyUser(verificationData)
    }

    override fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue {
        return native.registerUser(registrationData)
    }

    override fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference) {
        return native.FreeRegistrationDataPOSTContents(registrationData)
    }

    override fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference) {
        return native.FreeVerificationDataPOSTContents(verificationDataPOST)
    }
}
