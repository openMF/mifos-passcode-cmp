package com.mifos.passcode.mock_server.models

import com.mifos.passcode.auth.deviceAuth.WindowsAuthenticatorData
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.getAttestationObject
import com.mifos.passcode.mock_server.utils.getAuthenticatorDataAuthenticationExtensionAuthenticatorOutput
import com.mifos.passcode.mock_server.utils.getCollectdClientDataBytes
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.AuthenticationData
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.client.ClientDataType
import com.webauthn4j.data.client.CollectedClientData
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
        windowsAuthenticatorData: WindowsAuthenticatorData,
        userHandle: ByteArray,
        authenticatorDataBytes: ByteArray,
        signature: ByteArray,
        origin: String = "localhost",
        rpId: String = "localhost",
        type: ClientDataType = ClientDataType.WEBAUTHN_GET,
        challenge: String,
    ): Pair<Boolean, WindowsAuthenticatorData?>{

        lateinit var authenticationData: AuthenticationData

        val mOrigin = Origin(origin)
        val mChallenge = DefaultChallenge(challenge)

        val actualCollectedClientDataBytes = getCollectdClientDataBytes(origin, type.value, challenge)

        val collectedClientData = CollectedClientData(
            type,
            mChallenge,
            mOrigin,
            false,
            null
        )

        try {
//            authenticationData = webAuthManager.parseAuthenticationResponseJSON(authenticationResponse)
            authenticationData = AuthenticationData(
                windowsAuthenticatorData.credentialIdBytes,
                userHandle,
                getAuthenticatorDataAuthenticationExtensionAuthenticatorOutput(authenticatorDataBytes),
                authenticatorDataBytes,
                collectedClientData,
                actualCollectedClientDataBytes,
                null,
                signature
            )
        } catch (e: DataConversionException){
            println(e.message)
            throw e
        }


        val serverProperty = ServerProperty(
            mOrigin,
            rpId,
            mChallenge
        )

        val allowCredentials: MutableList<ByteArray?>? = null
        val userVerificationRequired = true
        val userPresenceRequired = true

        val credentialRecord = CredentialRecordImpl(
            getAttestationObject(windowsAuthenticatorData.attestationObjectBytes)!!,
            CollectedClientData(
                ClientDataType.WEBAUTHN_CREATE,
                DefaultChallenge(windowsAuthenticatorData.oldChallenge),
                mOrigin,
                null,
                null
            ),
            null,
            null
        )

        val authenticationParameters = AuthenticationParameters(
            serverProperty,
            credentialRecord,
            allowCredentials,
            userVerificationRequired,
            userPresenceRequired
        )

        val windowsAuthenticatorDataToSave = WindowsAuthenticatorData(
            windowsAuthenticatorData.attestationObjectBytes,
            actualCollectedClientDataBytes,
            windowsAuthenticatorData.credentialIdBytes,
            windowsAuthenticatorData.userId,
            challenge,
            counter = windowsAuthenticatorData.counter + 1
        )

        try {
            authenticationData = webAuthManager.verify(authenticationData, authenticationParameters)
            return Pair(true, windowsAuthenticatorDataToSave)
        }catch (e: VerificationException){
            e.printStackTrace()
            println("Personalized message: ")
            println(e.message)
            println(e.cause)
            println(e.localizedMessage)

            return Pair(false, null)
        }

    }

}


