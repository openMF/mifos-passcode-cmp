package com.mifos.passcode.mock_server.models

import com.mifos.passcode.mock_server.utils.generateChallenge
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.AuthenticationData
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.util.Base64UrlUtil
import com.webauthn4j.verifier.exception.VerificationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject


class UserAuthentication(){

    private val webAuthManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    fun generateClientData(
        challenge: String,
        origin: String = "localhost",
        type: String
    ): String {
        val clientDataJSONByteArray = "{challenge: $challenge, origin: $origin,type: $type}".toByteArray()

        return Base64UrlUtil.encodeToString(clientDataJSONByteArray)
    }

    fun generateAuthenticationRequestJSONString(
        origin: String = "localhost",
        timeout: Int = 60000,
        credentialId: String,
    ): String {

        val verificationJSON = buildJsonObject {
            put("challenge", JsonPrimitive(generateChallenge()))
            put("origin", JsonPrimitive(origin))
            put("timeout", JsonPrimitive(timeout))
            put("credentialId", JsonPrimitive(credentialId))
        }

        return Json.encodeToString(verificationJSON)
    }

    @Throws(DataConversionException::class)
    suspend fun verifyAuthenticationResponse(
        credentialRecord: CredentialRecordImpl,
        authenticationResponse: String,
        origin: String = "localhost",
        rpId: String = "localhost",
        challenge: String,
    ): Pair<Boolean, CredentialRecordImpl?>{

        lateinit var authenticationData: AuthenticationData

        try {
            authenticationData = webAuthManager.parseAuthenticationResponseJSON(authenticationResponse)
        } catch (e: DataConversionException){
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

        val allowCredentials: MutableList<ByteArray?>? = null
        val userVerificationRequired = true
        val userPresenceRequired = true


        val authenticationParameters = AuthenticationParameters(
            serverProperty,
            credentialRecord,
            allowCredentials,
            userVerificationRequired,
            userPresenceRequired
        )

        try {
            authenticationData = webAuthManager.verify(authenticationData, authenticationParameters)

            credentialRecord.counter = authenticationData.authenticatorData?.signCount ?: credentialRecord.counter

        }catch (e: VerificationException){

            println(e.localizedMessage)

            return Pair(false, null)
        }

        return Pair(true, credentialRecord)
    }

}

