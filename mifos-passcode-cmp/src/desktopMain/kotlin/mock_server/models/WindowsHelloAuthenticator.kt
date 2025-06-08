package com.mifos.passcode.mock_server.models

import auth.deviceAuth.windows.WindowsHelloAuthenticatorNativeSupportImpl
import com.mifos.passcode.mock_server.*
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.generateRandomUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.sun.jna.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


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
        accountName: String= "",
        displayName: String = "",
    ): WindowsAuthenticatorResponse.Registration {

        println("Entered the if statement")

        val challenge = generateChallenge()
        val userID = generateRandomUID()

        println(challenge)

        val registrationDataGET = RegistrationDataGET.ByReference()

        registrationDataGET.origin = "localhost"
        registrationDataGET.challenge = challenge
        registrationDataGET.timeout = 120000
        registrationDataGET.rpId = "localhost"
        registrationDataGET.rpName = "Mifos Initiative"
        registrationDataGET.userID = "yFDHoMO7pvCbKS9wrn-MHw"
        registrationDataGET.accountName = accountName.ifEmpty { "mifos@mifos.com" }
        registrationDataGET.displayName = displayName.ifEmpty { "MIFOS USER" }

        return withContext(Dispatchers.IO) {
            println("Entered withContext block, switching to IO thread.")
            var registrationDataPOST: RegistrationDataPOST.ByValue? = null
            try {
                println("Initiating registration.")
                registrationDataPOST = windowsHelloAuthenticator.registerUser(registrationDataGET)

                val windowsRegistrationResponse = WindowsRegistrationResponse(
                    registrationDataPOST.getAttestationObjectBytes() ?: byteArrayOf(),
                    registrationDataPOST.getCredentialIDBytes() ?: byteArrayOf(),
                    credentialIdLength = registrationDataPOST.credentialIdLength,
                    userId = registrationDataGET.userID,
                    windowsAuthenticationResponse = registrationDataPOST.getAuthenticationResult(),
                )
                println("Registration response: $windowsRegistrationResponse")
                WindowsAuthenticatorResponse.Registration.Success(windowsRegistrationResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                WindowsAuthenticatorResponse.Registration.Error
            } finally {
                println("Exiting registration block, freeing memory.")
                registrationDataPOST?.let {
                    windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(
                        registrationData = RegistrationDataPOST.ByReference(it.pointer)
                    )
                }
            }
        }
    }

    suspend fun invokeUserVerification(windowsRegistrationResponse: WindowsRegistrationResponse): WindowsAuthenticatorResponse.Verification {

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


        return withContext(Dispatchers.IO) {
            println("Entered withContext block, switching to IO thread.")
            var verificationDataPOST: VerificationDataPOST.ByValue? = null
            try {
                println("Initiating verification response verification.")
                verificationDataPOST = windowsHelloAuthenticator.verifyUser(verificationDataGET)

                val verificationResponse = verificationDataPOST.getVerificationResult()
                println("Verification successful")
                println("Verification response: $verificationResponse")
                WindowsAuthenticatorResponse.Verification.Success(verificationResponse)
            }catch (e: Exception){
                e.printStackTrace()
                nativeCredID.clear()
                WindowsAuthenticatorResponse.Verification.Error
            }finally {
                println("Exiting the verification block")
                nativeCredID.clear()
                verificationDataPOST?.let {
                    windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationDataPOST.pointer),)
                }
            }
        }
    }
}