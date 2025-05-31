package com.mifos.passcode.mock_server.models

import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.generateRandomUID
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.PublicKeyCredentialParameters
import com.webauthn4j.data.PublicKeyCredentialType
import com.webauthn4j.data.RegistrationData
import com.webauthn4j.data.RegistrationParameters
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.verifier.exception.VerificationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject


class UserRegistration(){

    private val webAuthManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    fun generateRegistrationRequestJSONString(
        origin: String = "localhost",       // website domain
        rpId: String = "localhost",         // website domain name
        rpName: String = "localhost",       //App/WebSite Name
        timeout: Int = 60000,
        accountName: String = "mifos@mifos.user",
        displayName: String = "Mifos User"
    ): String {

        val registrationJSON = buildJsonObject {
            put("challenge", JsonPrimitive(generateChallenge()))
            put("origin", JsonPrimitive(origin))
            put("rpId", JsonPrimitive(rpId))
            put("rpName", JsonPrimitive(rpId))
            put("timeout", JsonPrimitive(timeout))
            put("userID", JsonPrimitive(generateRandomUID()))
            put("accountName", JsonPrimitive(accountName))
            put("displayName", JsonPrimitive(displayName))
        }

        return Json.encodeToString(registrationJSON)
    }

    @Throws(DataConversionException::class)
    suspend fun verifyRegistrationResponse(
        registrationResponse: String,
        origin: String = "localhost",
        rpId: String = "localhost",
        challenge: String,
    ): Pair<Boolean, CredentialRecordImpl?>{
        var registrationData: RegistrationData? = null

        try {
            registrationData = webAuthManager.parseRegistrationResponseJSON(registrationResponse)
        }catch (e: DataConversionException){
            println(e.message)
            throw e
        }

        val oOrigin = Origin(origin)
        val oChallenge = DefaultChallenge(challenge)

        val serverProperty = ServerProperty(
            oOrigin,
            rpId,
            oChallenge
        )

        val pubKeyCredParams: List<PublicKeyCredentialParameters> = listOf(
            PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY,
                COSEAlgorithmIdentifier.ES256
            ),
            PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY,
                COSEAlgorithmIdentifier.RS256
            )
        )
        val userVerificationRequired = false
        val userPresenceRequired = true


        val registrationParams = RegistrationParameters(
            serverProperty,
            pubKeyCredParams,
            userVerificationRequired,
            userPresenceRequired
        )

        try {
            registrationData = webAuthManager.verify(registrationData, registrationParams)
        }catch (e: VerificationException){
            println(e.localizedMessage)
            return Pair(false, null)
        }


        val credentialRecord= CredentialRecordImpl(
            registrationData.attestationObject!!,
            registrationData.collectedClientData,
            registrationData.clientExtensions,
            registrationData.transports
        )

        return Pair(true, credentialRecord)
    }

}



fun saveEncodedData(
    credentialRecord: CredentialRecordImpl
): String {
    return Json.encodeToString(credentialRecord)
}

fun decodeSavedData(jsonString: String): CredentialRecordImpl{
    return Json.decodeFromString(jsonString)
}