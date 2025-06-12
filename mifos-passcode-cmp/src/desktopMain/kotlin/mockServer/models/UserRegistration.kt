//package com.mifos.passcode.mock_server.models
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import com.mifos.passcode.auth.deviceAuth.WindowsAuthenticatorData
//import com.mifos.passcode.mock_server.utils.getAttestationObject
//import com.mifos.passcode.mock_server.utils.getCollectdClientDataBytes
//import com.webauthn4j.WebAuthnManager
//import com.webauthn4j.credential.CredentialRecordImpl
//import com.webauthn4j.data.PublicKeyCredentialParameters
//import com.webauthn4j.data.PublicKeyCredentialType
//import com.webauthn4j.data.RegistrationData
//import com.webauthn4j.data.RegistrationParameters
//import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
//import com.webauthn4j.data.client.ClientDataType
//import com.webauthn4j.data.client.CollectedClientData
//import com.webauthn4j.data.client.Origin
//import com.webauthn4j.data.client.challenge.DefaultChallenge
//import com.webauthn4j.server.ServerProperty
//import com.webauthn4j.verifier.exception.VerificationException
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//
//
//class UserRegistration(){
//
//    private val webAuthManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()
//
//    companion object {
//        private val logger = org.slf4j.LoggerFactory.getLogger(UserRegistration::class.java)
//    }
//
//    @OptIn(ExperimentalStdlibApi::class)
//    fun verifyRegistrationResponse(
//        attestationObjectBytes: ByteArray,
//        credentialIdBytes: ByteArray,
//        credentialIdLength: Int,
//        userId: String,
//        origin: String = "localhost",
//        type: ClientDataType = ClientDataType.WEBAUTHN_CREATE,
//        rpId: String = "localhost",
//        challenge: String,
//    ): Pair<Boolean, WindowsAuthenticatorData?>{
//
//        println("Challenge $challenge")
//
//        val attestationObject = getAttestationObject(attestationObjectBytes)
//        println(attestationObject)
//
//        if(attestationObject == null){
//            println("Failed to parse attestation Object")
//            return Pair(false, null)
//        }
//
//        val actualCollectedClientDataBytes = getCollectdClientDataBytes(origin, type.value, challenge)
//
//        val mOrigin = Origin(origin)
//        val mChallenge = DefaultChallenge(challenge)
//
//        val collectedClientData = CollectedClientData(
//            type,
//            mChallenge,
//            mOrigin,
//            false,
//            null
//        )
//
//        var registrationData: RegistrationData? by mutableStateOf(null)
//
//        registrationData = RegistrationData(
//            attestationObject,
//            attestationObjectBytes,
//            collectedClientData,
//            actualCollectedClientDataBytes,
//            null,
//            null
//        )
//
//        val x = registrationData?.clientExtensions
//
//        println("AuthenticationExtensions: $x")
//
//        println("Data used for verification: ")
//        println("CollectedClientData: ${registrationData?.collectedClientData}")
//        println("AttestationObjectBytes: ${registrationData?.attestationObjectBytes?.toHexString()}")
//        println("CollectedClientDataBytes: ${registrationData?.collectedClientDataBytes?.toHexString()}")
//        println("AttestationObject: ${registrationData?.attestationObject}")
//
////        var registrationData =  webAuthManager.parseRegistrationResponseJSON(registrationJson)
//
//        val serverProperty = ServerProperty(
//            mOrigin,
//            rpId,
//            mChallenge
//        )
//
//        val pubKeyCredParams: List<PublicKeyCredentialParameters> = listOf(
//            PublicKeyCredentialParameters(
//                PublicKeyCredentialType.PUBLIC_KEY,
//                COSEAlgorithmIdentifier.ES256
//            ),
//            PublicKeyCredentialParameters(
//                PublicKeyCredentialType.PUBLIC_KEY,
//                COSEAlgorithmIdentifier.RS256
//            )
//        )
//        val userVerificationRequired = true
//        val userPresenceRequired = true
//
//        val registrationParams = RegistrationParameters(
//            serverProperty,
//            pubKeyCredParams,
//            userVerificationRequired,
//            userPresenceRequired
//        )
//
//        try {
//            println("Doing verification")
//            registrationData = webAuthManager.verify(registrationData!!, registrationParams)
//
//            val credRec = CredentialRecordImpl(
//                registrationData!!.attestationObject!!,
//                registrationData!!.collectedClientData!!,
//                null,
//                null
//            )
//
//            val windowsAuthenticatorData = WindowsAuthenticatorData(
//                attestationObjectBytes,
//                actualCollectedClientDataBytes,
//                credentialIdBytes = credentialIdBytes,
//                credentialIdLength,
//                userId,
//                challenge,
//                counter = credRec.counter
//            )
//
//            println(windowsAuthenticatorData)
//
//            return Pair(true, windowsAuthenticatorData)
//
//        } catch (e: VerificationException) {
//            logger.error("!!! WEBAuthN4J VERIFICATION EXCEPTION CAUGHT !!!") // Use System.err for errors
//            logger.error(">>> Exception Type: ${e.javaClass.name}")
//            logger.error(">>> Exception Message: ${e.message}")
//            // It's crucial to see the full stack trace
//            e.printStackTrace(System.err) // Explicitly direct to System.err
//            System.err.flush()          // Force flush the error stream
//
//            // Also flush System.out if you used println to System.out
//            System.out.flush()
//
//            logger.error("Verification failed in UserRegistration.verifyRegistrationResponse due to webauthn4j error.") // This might go to System.out
//            System.out.flush() // Flush System.out too
//            return Pair(false, null)
//        } catch (e: Exception) { // Catch any other unexpected errors
//            logger.error("!!! UNEXPECTED EXCEPTION DURING VERIFICATION !!!")
//            logger.error(">>> Exception Type: ${e.javaClass.name}")
//            logger.error(">>> Exception Message: ${e.message}")
//            e.printStackTrace(System.out)
//            System.out.flush()
//            logger.error("Verification failed due to unexpected error.")
//            return Pair(false, null)
//        }
//
//    }
//
//}
//
//
//
//fun saveEncodedData(
//    credentialRecord: CredentialRecordImpl
//): String {
//    return Json.encodeToString(credentialRecord)
//}
//
//fun decodeSavedData(jsonString: String): CredentialRecordImpl{
//    return Json.decodeFromString(jsonString)
//}
//
