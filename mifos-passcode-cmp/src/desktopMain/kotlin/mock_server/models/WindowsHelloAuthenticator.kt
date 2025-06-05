package com.mifos.passcode.mock_server.models

import com.mifos.passcode.auth.deviceAuth.*
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.generateRandomUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.sun.jna.Memory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class RegistrationResponse(
    val attestationObjectBytes: ByteArray,
    val credentialIdBytes: ByteArray,
    val credentialIdLength: Int,
    val userId: String,
    val authenticationResponse: AuthenticationResponse
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegistrationResponse

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
        class Success(val response: RegistrationResponse) : Registration()
        class Error : Registration()
    }
    sealed class Verification{
        class Success(val response: AuthenticationResponse) : Verification()
        class Error : Verification()
    }
}

class WindowsHelloAuthenticator(
    private val windowsHelloAuthenticator: WindowsHelloAuthenticatorNativeSupportImpl,
) {
    private var scope = CoroutineScope(Dispatchers.Default)

    // Determines whether the platform authenticator service is available.
    fun checkIfWindowsHelloSupportedOrNot() = windowsHelloAuthenticator.checkIfAuthenticatorIsAvailable()

    suspend fun invokeUserRegistration(): WindowsAuthenticatorResponse.Registration {
        lateinit var registrationDataPOST: RegistrationDataPOST.ByValue

        println("Entered the if statement")

        val challenge = generateChallenge()
        val userID = generateRandomUID()

        println(challenge)

        val registrationDataGET = RegistrationDataGET.ByReference()

        registrationDataGET.origin = "localhost"
        registrationDataGET.challenge = challenge
        registrationDataGET.timeout = 120000
        registrationDataGET.rpId = "localhost"
        registrationDataGET.rpName = "MIFOS"
        registrationDataGET.userID = "yFDHoMO7pvCbKS9wrn-MHw"
        registrationDataGET.accountName = "mifos@mifos.com"
        registrationDataGET.displayName = "MIFOS USER"


        val registrationJob = scope.async(Dispatchers.IO) {
            println("Entered first async block")
            try {
                println("Initiating registration.")
                registrationDataPOST = windowsHelloAuthenticator.registerUser(registrationDataGET)
            }catch (e: Exception){
                e.printStackTrace()
                windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationDataPOST.pointer))
                return@async WindowsAuthenticatorResponse.Registration.Error()
            }finally {
                println("Exiting the registration block")
            }
        }
        registrationJob.await()

        val registrationResponse = RegistrationResponse(
            registrationDataPOST.getAttestationObjectBytes()!!,
            registrationDataPOST.getCredentialIDBytes()!!,
            credentialIdLength = registrationDataPOST.credentialIdLength,
            userId = registrationDataGET.userID,
            authenticationResponse = registrationDataPOST.getAuthenticationResult(),
        )

        windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationDataPOST.pointer))
        return WindowsAuthenticatorResponse.Registration.Success(response = registrationResponse)
    }

    suspend fun invokeUserVerification(registrationResponse: RegistrationResponse): WindowsAuthenticatorResponse.Verification {
        lateinit var verificationDataPOST: VerificationDataPOST.ByValue

        val challenge = generateChallenge()

        println(challenge)

        val verificationDataGET = VerificationDataGET.ByReference()

        val nativeCredID = Memory(registrationResponse.credentialIdBytes.size.toLong())

        nativeCredID.write(0, registrationResponse.credentialIdBytes,0,registrationResponse.credentialIdBytes.size)

        verificationDataGET.origin = "localhost"
        verificationDataGET.challenge = challenge
        verificationDataGET.userID = nativeCredID
        verificationDataGET.userIDLength = registrationResponse.credentialIdBytes.size.toLong()
        verificationDataGET.rpId = "localhost"
        verificationDataGET.timeout = 120000

        scope.async(Dispatchers.IO) {
            println("Entered first async block for user verification")
            try {
                println("Initiating verification response verification.")
                verificationDataPOST = windowsHelloAuthenticator.verifyUser(verificationDataGET)
                println("Verification successful")
            }catch (e: Exception){
                e.printStackTrace()
                nativeCredID.clear()
                windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationDataPOST.pointer),)
                return@async WindowsAuthenticatorResponse.Verification.Error()
            }finally {
                println("Exiting the verification block")
            }
        }.await()

        val verificationResponse = verificationDataPOST.getVerificationResult()
        nativeCredID.clear()
        windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationDataPOST.pointer),)
        return WindowsAuthenticatorResponse.Verification.Success(verificationResponse)
    }
}

final class WindowsAuthenticatorDataBase(){
    private val dataStore by lazy {
        Settings()
    }

    fun saveRegistrationResponse(registrationResponse: RegistrationResponse){
        val stringifiedCredRecords = Json.encodeToString(registrationResponse)
        dataStore.putString(CREDENTIAL_RECORD_KEY, stringifiedCredRecords)
    }

    fun getRegistrationResponse(): RegistrationResponse{
        val fetchedData = dataStore[CREDENTIAL_RECORD_KEY, ""]
        return Json.decodeFromString(fetchedData)
    }

    fun removeRegistrationResponse(){
        dataStore.remove(CREDENTIAL_RECORD_KEY)
    }
}

