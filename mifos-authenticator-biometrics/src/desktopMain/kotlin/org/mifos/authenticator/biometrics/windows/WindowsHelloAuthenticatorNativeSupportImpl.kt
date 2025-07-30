package org.mifos.authenticator.biometrics.windows

import org.mifos.authenticator.biometrics.mockServer.RegistrationDataGET
import org.mifos.authenticator.biometrics.mockServer.RegistrationDataPOST
import org.mifos.authenticator.biometrics.mockServer.VerificationDataGET
import org.mifos.authenticator.biometrics.mockServer.VerificationDataPOST
import com.sun.jna.Native

final class WindowsHelloAuthenticatorNativeSupportImpl: WindowsHelloAuthenticatorNativeSupport {

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

