package auth.deviceAuth.windows

import com.mifos.passcode.mockServer.RegistrationDataGET
import com.mifos.passcode.mockServer.RegistrationDataPOST
import com.mifos.passcode.mockServer.VerificationDataGET
import com.mifos.passcode.mockServer.VerificationDataPOST
import com.sun.jna.Library

/**
 * Interface for accessing function from native C code.
 * Function name cannot be changed in the interface unless changes in the native C code.
 * WindowsHelloAuthenticator.dll provide the C code/
 * C code files present in the nativeCode directory are only for the sake of keeping everything open source.
 * They don't do anything.
*/
interface WindowsHelloAuthenticatorNativeSupport: Library {

    fun checkIfAuthenticatorIsAvailable(): Boolean

    fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue

    fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue

    fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference)

    fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference)
}
