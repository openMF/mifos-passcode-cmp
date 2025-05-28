package com.mifos.passcode.mock_server.utils

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