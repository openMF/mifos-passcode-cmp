package com.mifos.passcode.mockServer

import com.sun.jna.Pointer
import com.sun.jna.Structure

enum class WindowsAuthenticationResponse {
    SUCCESS,
    UNSUCCESSFUL,
    MEMORY_ALLOCATION_ERROR,
    E_FAILURE,
    ABORTED,
    USER_CANCELED,
    REGISTER_AGAIN,
    UNKNOWN_ERROR,
    INVALID_PARAMETER,
}


sealed class RetrievedDataFromAuthenticator {
    class Success(val bytes: ByteArray) : RetrievedDataFromAuthenticator()
    class Error(val message: String) : RetrievedDataFromAuthenticator()
}


fun mapAuthenticationResponseENUM(authenticationResponse: Long): WindowsAuthenticationResponse {
    return when (authenticationResponse) {
        1L -> {
            WindowsAuthenticationResponse.SUCCESS
        } // 0x00000000 Success code from windows hello
        0L -> {
            WindowsAuthenticationResponse.UNSUCCESSFUL
        } // 0x00000001 failed code from windows hello
        99999999L -> {
            WindowsAuthenticationResponse.MEMORY_ALLOCATION_ERROR
        }
        80004005L -> {
            WindowsAuthenticationResponse.E_FAILURE
        }
        80004004L -> {
            WindowsAuthenticationResponse.ABORTED
        }
        80090036L -> {
            WindowsAuthenticationResponse.USER_CANCELED
        }
        80090027L -> {
            WindowsAuthenticationResponse.INVALID_PARAMETER
        }
        800900013L -> {
            WindowsAuthenticationResponse.REGISTER_AGAIN
        } // 0x8009000D NTE_NO_KEY error from windows hello ( credentialId is no longer valid )
        800015151515L -> {
            WindowsAuthenticationResponse.UNKNOWN_ERROR
        } // 0x8000FFFF Error code from windows hello
        else -> WindowsAuthenticationResponse.UNKNOWN_ERROR
    }
}

@Structure.FieldOrder(
    "authenticatorDataBytes",
    "authenticatorDataLength",
    "signatureDataBytes",
    "signatureDataBytesLength",
    "userHandle",
    "userHandleLength",
    "origin",
    "challenge",
    "type",
    "authenticationResult"
)
open class VerificationDataPOST : Structure {
    @JvmField var authenticatorDataBytes: Pointer? = null

    @JvmField var authenticatorDataLength: Int = 0

    @JvmField var signatureDataBytes: Pointer? = null

    @JvmField var signatureDataBytesLength: Int = 0

    @JvmField var userHandle: Pointer? = null

    @JvmField var userHandleLength: Int = 0

    @JvmField var origin: String = ""

    @JvmField var challenge: String = ""

    @JvmField var type: String = ""

    @JvmField var authenticationResult: Long = 0

    constructor() : super()
    constructor(p: Pointer?) : super(p)

    fun getVerificationResult(): WindowsAuthenticationResponse {
        return mapAuthenticationResponseENUM(authenticationResult)
    }

    /**
     * Below functions were once used in the UserRegistration and UserAuthentication class
     * but since they are commented I am not using them anymore.
     * When doing web implementation if I don't find use for them then I will
     * remove them.
     */

    fun getAuthenticatorDataBytes(): RetrievedDataFromAuthenticator {
        val bytes = authenticatorDataBytes
        return if (bytes != null) {
            if (authenticatorDataLength <= 0) {
                RetrievedDataFromAuthenticator.Error("Received invalid authenticatorData. Registration failed")
            } else {
                RetrievedDataFromAuthenticator.Success(
                    bytes.getByteArray(0, authenticatorDataLength)
                )
            }
        } else {
            RetrievedDataFromAuthenticator.Error("Received invalid authenticatorData. Registration failed")
        }
    }

    fun getSignatureDataBytes(): RetrievedDataFromAuthenticator {
        val bytes = signatureDataBytes
        return if (bytes != null) {
            if (signatureDataBytesLength <= 0) {
                RetrievedDataFromAuthenticator.Error("Received invalid signatureData. Registration failed")
            } else {
                RetrievedDataFromAuthenticator.Success(
                    bytes.getByteArray(0, signatureDataBytesLength)
                )
            }
        } else {
            RetrievedDataFromAuthenticator.Error("Received invalid signatureData. Registration failed")
        }
    }

    fun getUserHandleBytes(): RetrievedDataFromAuthenticator {
        val bytes = userHandle
        return if (bytes != null) {
            if (userHandleLength <= 0) {
                RetrievedDataFromAuthenticator.Error("Received invalid signatureData. Registration failed")
            } else {
                RetrievedDataFromAuthenticator.Success(
                    bytes.getByteArray(0, userHandleLength)
                )
            }
        } else {
            RetrievedDataFromAuthenticator.Error("Received invalid signatureData. Registration failed")
        }
    }

    class ByValue : VerificationDataPOST(), Structure.ByValue

    class ByReference : VerificationDataPOST, Structure.ByReference {
        constructor(p: Pointer?) : super(p) {}
    }
}

@Structure.FieldOrder("origin", "userID", "userIDLength", "challenge", "rpId", "timeout")
open class VerificationDataGET : Structure {
    @JvmField var origin: String = ""

    @JvmField var userID: Pointer? = null

    @JvmField var userIDLength: Long = 0

    @JvmField var challenge: String = ""

    @JvmField var rpId: String = ""

    @JvmField var timeout: Int = 120000

    constructor() : super()

    class ByReference : VerificationDataGET(), Structure.ByReference
}

@Structure.FieldOrder("origin", "challenge", "timeout", "rpId", "rpName", "userID", "accountName", "displayName")
open class RegistrationDataGET : Structure {
    @JvmField var origin: String = ""

    @JvmField var challenge: String = ""

    @JvmField var timeout: Int = 60000

    @JvmField var rpId: String = ""

    @JvmField var rpName: String = ""

    @JvmField var userID: String = ""

    @JvmField var accountName: String = ""

    @JvmField var displayName: String = ""

    constructor() : super()

    class ByReference : RegistrationDataGET(), Structure.ByReference
}

@Structure.FieldOrder(
    "attestationObjectBytes",
    "attestationObjectLength",
    "credentialIdBytes",
    "credentialIdLength",
    "origin",
    "type",
    "challenge",
    "authenticationResult"
)
open class RegistrationDataPOST : Structure {
    @JvmField var attestationObjectBytes: Pointer? = null

    @JvmField var attestationObjectLength: Int = 0

    @JvmField var credentialIdBytes: Pointer? = null

    @JvmField var credentialIdLength: Int = 0

    @JvmField var origin: String = ""

    @JvmField var type: String = "webauthn.create"

    @JvmField var challenge: String = ""

    @JvmField var authenticationResult: Long = 0

    constructor() : super()
    constructor(p: Pointer?) : super(p)

    fun getAuthenticationResult(): WindowsAuthenticationResponse {
        return mapAuthenticationResponseENUM(authenticationResult)
    }

    override fun toString(): String {
        return "RegistrationDataPost(\n" +
            "   ${attestationObjectBytes}\n" +
            "   $attestationObjectLength\n" +
            "   $credentialIdBytes\n" +
            "   $credentialIdLength\n" +
            "   $origin\n" +
            "   $type\n" +
            "   $challenge\n" +
            "   $authenticationResult\n" +
            ")"
    }

    fun getAttestationObjectBytes(): RetrievedDataFromAuthenticator {
        val bytes = attestationObjectBytes
        return if (bytes != null) {
            if (attestationObjectLength <= 0) {
                RetrievedDataFromAuthenticator.Error("Received invalid credentialId. Registration failed")
            } else {
                RetrievedDataFromAuthenticator.Success(
                    bytes.getByteArray(0, attestationObjectLength)
                )
            }
        } else {
            RetrievedDataFromAuthenticator.Error("Received invalid credentialId. Registration failed")
        }
    }

    fun getCredentialIDBytes(): RetrievedDataFromAuthenticator {
        val bytes = credentialIdBytes
        return if (bytes != null) {
            if (credentialIdLength <= 0) {
                RetrievedDataFromAuthenticator.Error("Received invalid credentialId. Registration failed")
            } else {
                RetrievedDataFromAuthenticator.Success(
                    bytes.getByteArray(0, credentialIdLength)
                )
            }
        } else {
            RetrievedDataFromAuthenticator.Error("Receved invalid credentialId. Registration failed")
        }
    }

    class ByValue : RegistrationDataPOST(), Structure.ByValue

    class ByReference : RegistrationDataPOST, Structure.ByReference {
        constructor(p: Pointer?) : super(p)
    }
}
