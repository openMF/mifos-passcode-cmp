package com.mifos.passcode.mockServer.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.webauthn4j.converter.AttestationObjectConverter
import com.webauthn4j.converter.AuthenticatorDataConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.data.attestation.AttestationObject
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionAuthenticatorOutput
import com.webauthn4j.util.Base64UrlUtil
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.SecureRandom


fun generateChallenge(): String {
    val secureRandom = SecureRandom()
    val challenge = ByteArray(32)
    secureRandom.nextBytes(challenge)
    val base64UrlChallenge = Base64UrlUtil.encodeToString(challenge);

    return base64UrlChallenge
}


fun generateRandomUID(): String{
    val secureRandom = SecureRandom()
    val randomUID = ByteArray(16)
    secureRandom.nextBytes(randomUID)

    return Base64UrlUtil.encodeToString(randomUID)
}

fun generateBase64EncodedUID(userId: String): String{
    val secureRandom = SecureRandom()
    val userIDBytes = userId.toByteArray()
    secureRandom.nextBytes(userIDBytes)

    return Base64UrlUtil.encodeToString(userIDBytes)
}

fun getAttestationObject(attestationObjectBytes: ByteArray): AttestationObject? {

    val cborFactory = CBORFactory()
    val jsonFac = JsonFactory()
    val cborMapper = ObjectMapper(cborFactory)
    val jsonMapper  = ObjectMapper(jsonFac)
    val objectConverter = ObjectConverter(jsonMapper, cborMapper)
    val attestationObjectConverter = AttestationObjectConverter(objectConverter)

    val attestationObject = attestationObjectConverter.convert(attestationObjectBytes)

    return attestationObject
}

fun getCollectdClientDataBytes(origin: String, type: String, challenge: String): ByteArray {
    val clientDataJsonString = buildJsonObject {
        put("type", type)
        put("challenge", challenge)
        put("origin", origin)
    }.toString()

    return clientDataJsonString.toByteArray(Charsets.UTF_8)
}

fun getAuthenticatorDataAuthenticationExtensionAuthenticatorOutput(authenticationDataBytes: ByteArray): AuthenticatorData<AuthenticationExtensionAuthenticatorOutput> {

    val cborFactory = CBORFactory()
    val jsonFac = JsonFactory()
    val cborMapper = ObjectMapper(cborFactory)
    val jsonMapper  = ObjectMapper(jsonFac)
    val objectConverter = ObjectConverter(jsonMapper, cborMapper)
    val authenticatorDataConverter = AuthenticatorDataConverter(objectConverter)

    val authenticatorData: AuthenticatorData<AuthenticationExtensionAuthenticatorOutput> = authenticatorDataConverter.convert(authenticationDataBytes)

    return authenticatorData
}

