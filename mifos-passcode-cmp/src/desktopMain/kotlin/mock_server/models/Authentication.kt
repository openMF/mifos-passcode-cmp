package com.mifos.passcode.mock_server.models

import androidx.compose.ui.graphics.Paint
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.authenticator.Authenticator
import com.webauthn4j.authenticator.AuthenticatorImpl
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CoreCredentialRecordImpl
import com.webauthn4j.credential.CredentialRecord
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


fun generateClientData(
    challenge: String,
    origin: String,
    type: String
): String {
    val clientDataJSONByteArray = "{challenge: $challenge, origin: $origin,type: $type}".toByteArray()

    return Base64UrlUtil.encodeToString(clientDataJSONByteArray)
}


suspend fun generateAuthenticationRequestJSONString(
    origin: String,
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
    credentialRecord: CredentialRecord,
    webAuthManager: WebAuthnManager,
    authenticationResponse: String,
    origin: String,
    rpId: String,
    challenge: String,
): Pair<Boolean, AuthenticatorImpl?>{

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

    val authenticator = AuthenticatorImpl(
        credentialRecord.attestedCredentialData,
        credentialRecord.attestationStatement,
        credentialRecord.counter
    )

    val authenticationParameters = AuthenticationParameters(
        serverProperty,
        authenticator,
        allowCredentials,
        userVerificationRequired,
        userPresenceRequired
    )



    try {
        authenticationData = webAuthManager.verify(authenticationData, authenticationParameters)
    }catch (e: VerificationException){

        println(e.localizedMessage)

        return Pair(false, null)
    }

    return Pair(true, authenticator)
}