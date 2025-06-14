package com.mifos.passcode.auth.deviceAuth.windows

import auth.deviceAuth.windows.WindowsHelloAuthenticatorNativeSupportImpl
import com.mifos.passcode.mockServer.RegistrationDataGET
import com.mifos.passcode.mockServer.RegistrationDataPOST
import com.mifos.passcode.mockServer.VerificationDataGET
import com.mifos.passcode.mockServer.VerificationDataPOST
import com.mifos.passcode.mockServer.WindowsAuthenticationResponse
import com.mifos.passcode.mockServer.utils.generateBase64EncodedUID
import com.mifos.passcode.mockServer.utils.generateChallenge
import com.mifos.passcode.mockServer.utils.generateRandomUID
import com.sun.jna.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


@Serializable
data class WindowsRegistrationResponse(
    val attestationObjectBytes: ByteArray,
    val credentialIdBytes: ByteArray,
    val credentialIdLength: Int,
    val userId: String,
    val windowsAuthenticationResponse: WindowsAuthenticationResponse
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowsRegistrationResponse

        if (credentialIdLength != other.credentialIdLength) return false
        if (!attestationObjectBytes.contentEquals(other.attestationObjectBytes)) return false
        if (!credentialIdBytes.contentEquals(other.credentialIdBytes)) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credentialIdLength
        result = 31 * result + attestationObjectBytes.contentHashCode()
        result = 31 * result + credentialIdBytes.contentHashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}

sealed class WindowsAuthenticatorResponse{
    sealed class Registration{
        class Success(val response: WindowsRegistrationResponse) : Registration()
        data object Error : Registration()
    }
    sealed class Verification{
        class Success(val response: WindowsAuthenticationResponse) : Verification()
        data object Error : Verification()
    }
}

class WindowsHelloAuthenticator(
    private val windowsHelloAuthenticator: WindowsHelloAuthenticatorNativeSupportImpl,
) {

    // Determines whether the platform authenticator service is available.
    fun checkIfWindowsHelloSupportedOrNot() = windowsHelloAuthenticator.checkIfAuthenticatorIsAvailable()

    suspend fun invokeUserRegistration(
        userId: String ="",
        accountName: String= "",
        displayName: String = "",
    ): WindowsAuthenticatorResponse.Registration {

        return withContext(Dispatchers.IO) {
            println("Entered withContext block, switching to IO thread.")

            val challenge = generateChallenge()

            println(challenge)

            val registrationDataGET = RegistrationDataGET.ByReference()

            registrationDataGET.origin = "localhost"
            registrationDataGET.challenge = challenge
            registrationDataGET.timeout = 120000
            registrationDataGET.rpId = "localhost"
            registrationDataGET.rpName = "Mifos Initiative"
            registrationDataGET.userID = if(userId.isEmpty()) generateRandomUID() else generateBase64EncodedUID(userId)
            registrationDataGET.accountName = accountName.ifEmpty { "mifos@mifos.com" }
            registrationDataGET.displayName = displayName.ifEmpty { "MIFOS USER" }

            var registrationDataPOST: RegistrationDataPOST.ByValue? = null
            try {
                registrationDataPOST = windowsHelloAuthenticator.registerUser(registrationDataGET)

                val windowsRegistrationResponse = WindowsRegistrationResponse(
                    registrationDataPOST.getAttestationObjectBytes() ?: byteArrayOf(),
                    registrationDataPOST.getCredentialIDBytes() ?: byteArrayOf(),
                    credentialIdLength = registrationDataPOST.credentialIdLength,
                    userId = registrationDataGET.userID,
                    windowsAuthenticationResponse = registrationDataPOST.getAuthenticationResult(),
                )
                WindowsAuthenticatorResponse.Registration.Success(windowsRegistrationResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                WindowsAuthenticatorResponse.Registration.Error
            } finally {
                registrationDataPOST?.let {
                    windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(
                        registrationData = RegistrationDataPOST.ByReference(it.pointer)
                    )
                }
                registrationDataPOST = null
            }
        }
    }

    suspend fun invokeUserVerification(windowsRegistrationResponse: WindowsRegistrationResponse): WindowsAuthenticatorResponse.Verification {

        return withContext(Dispatchers.IO) {

            val challenge = generateChallenge()

            println(challenge)

            val verificationDataGET = VerificationDataGET.ByReference()

            val nativeCredID = Memory(windowsRegistrationResponse.credentialIdBytes.size.toLong())

            nativeCredID.write(0, windowsRegistrationResponse.credentialIdBytes,0,windowsRegistrationResponse.credentialIdBytes.size)

            verificationDataGET.origin = "localhost"
            verificationDataGET.challenge = challenge
            verificationDataGET.userID = nativeCredID
            verificationDataGET.userIDLength = windowsRegistrationResponse.credentialIdBytes.size.toLong()
            verificationDataGET.rpId = "localhost"
            verificationDataGET.timeout = 120000

            var verificationDataPOST: VerificationDataPOST.ByValue? = null
            try {
                verificationDataPOST = windowsHelloAuthenticator.verifyUser(verificationDataGET)

                val verificationResponse = verificationDataPOST.getVerificationResult()
                println("Verification response: $verificationResponse")
                WindowsAuthenticatorResponse.Verification.Success(verificationResponse)
            }catch (e: Exception){
                e.printStackTrace()
                WindowsAuthenticatorResponse.Verification.Error
            }finally {
                verificationDataPOST?.let {
                    windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(it.pointer))
                }
                verificationDataPOST = null
                nativeCredID.close()
            }
        }
    }
}