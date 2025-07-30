package org.mifos.authenticator.biometrics.mockServer.utils

import com.webauthn4j.util.Base64UrlUtil
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