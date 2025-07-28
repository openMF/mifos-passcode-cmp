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
interface WindowsHelloAuthenticatorNativeSupport : Library {

    /**
     * Checks if the Windows Hello authenticator is available on the current system.
     *
     * This native function queries the operating system to determine if the necessary
     * hardware (e.g., fingerprint reader, IR camera) and software components for
     * Windows Hello are present and operational.
     *
     * @return `true` if Windows Hello is supported and available, `false` otherwise.
     */
    fun checkIfAuthenticatorIsAvailable(): Boolean

    /**
     * Initiates the user verification (authentication) process with Windows Hello.
     *
     * This function prompts the user to authenticate using their enrolled Windows Hello
     * credential. It takes input verification data, including the credential ID of the
     * user to be authenticated.
     *
     * @param verificationData An instance of [VerificationDataGET.ByReference] containing
     * parameters for the verification request, such as the credential ID
     * to verify, RP ID, and challenge.
     * @return A [VerificationDataPOST.ByValue] object containing the response from the
     * native verification process, including the authentication result.
     */
    fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue

    /**
     * Initiates the user registration process with Windows Hello.
     *
     * This function communicates with the native Windows Hello API to prompt the user
     * to enroll a new biometric or PIN credential. It takes input registration data
     * and returns the result of the registration, including the generated credential ID.
     *
     * @param registrationData An instance of [RegistrationDataGET.ByReference] containing
     * all the necessary parameters for the registration request,
     * such as RP ID, user details, and challenge.
     * @return A [RegistrationDataPOST.ByValue] object containing the response from the
     * native registration process, including the attestation object and
     * credential ID.
     */
    fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue

    /**
     * Frees native memory allocated for a [RegistrationDataPOST] structure.
     *
     * It is crucial to call this function after processing a [RegistrationDataPOST]
     * returned by [registerUser] to prevent memory leaks in the native code.
     *
     * @param registrationData A [RegistrationDataPOST.ByReference] pointing to the
     * native memory that needs to be freed.
     */
    fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference)

    /**
     * Frees native memory allocated for a [VerificationDataPOST] structure.
     *
     * It is crucial to call this function after processing a [VerificationDataPOST]
     * returned by [verifyUser] to prevent memory leaks in the native code.
     *
     * @param verificationDataPOST A [VerificationDataPOST.ByReference] pointing to the
     * native memory that needs to be freed.
     */
    fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference)
}
