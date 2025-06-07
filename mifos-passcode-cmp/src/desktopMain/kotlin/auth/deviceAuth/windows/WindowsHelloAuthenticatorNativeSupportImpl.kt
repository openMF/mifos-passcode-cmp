package auth.deviceAuth.windows

import com.mifos.passcode.mock_server.*
import com.sun.jna.Native

final class WindowsHelloAuthenticatorNativeSupportImpl: WindowsHelloAuthenticatorNativeSupport{

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

