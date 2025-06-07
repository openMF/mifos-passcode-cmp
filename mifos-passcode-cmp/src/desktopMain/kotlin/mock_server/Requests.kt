package com.mifos.passcode.auth.deviceAuth

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.sun.jna.Pointer
import com.sun.jna.Structure

enum class AuthenticationResponse{
    SUCCESS,
    UNSUCCESSFUL,
    MEMORY_ALLOCATION_ERROR,
    E_FAILURE,
    ABORTED,
    USER_CANCELED,
    UNKNOWN_ERROR,
    INVALID_PARAMETER,
}

fun mapAuthenticationResponseENUM(authenticationResponse: Long): AuthenticationResponse{
    return when(authenticationResponse){
        1L -> {
            AuthenticationResponse.SUCCESS
        }
        0L -> {
            AuthenticationResponse.UNSUCCESSFUL
        }
        99999999L -> {
            AuthenticationResponse.MEMORY_ALLOCATION_ERROR
        }
        80004005L -> {
            AuthenticationResponse.E_FAILURE
        }
        80004004L -> {
            AuthenticationResponse.ABORTED
        }
        80090036L -> {
            AuthenticationResponse.USER_CANCELED
        }
        80090027L -> {
            AuthenticationResponse.INVALID_PARAMETER
        }
        800015151515L -> {
            AuthenticationResponse.UNKNOWN_ERROR
        }
        else -> AuthenticationResponse.UNKNOWN_ERROR
    }
}


@Structure.FieldOrder("authenticatorDataBytes", "authenticatorDataLength", "signatureDataBytes", "signatureDataBytesLength","userHandle", "userHandleLength" ,"origin", "challenge", "type", "authenticationResult")
open class VerificationDataPOST: Structure {
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
    constructor(p: Pointer?) : super(p) {}

    fun getVerificationResult(): AuthenticationResponse{
        return mapAuthenticationResponseENUM(authenticationResult)
    }

    fun getAuthenticatorDataBytes(): ByteArray? {
        if (authenticatorDataBytes == null || authenticatorDataLength <= 0) {
            println("Null AuthenticatorDataBytes")
            return null
        }
        return authenticatorDataBytes!!.getByteArray(0, authenticatorDataLength)
    }

    fun getSignatureDataBytes(): ByteArray? {
        if (signatureDataBytes == null || signatureDataBytesLength <= 0) {
            println("Null signatureDataBytes")
            return null
        }
        return signatureDataBytes!!.getByteArray(0, signatureDataBytesLength)

    }

    fun getUserHandleBytes(): ByteArray? {
        if (userHandle == null || userHandleLength <= 0) {
            println("Null user handle")
            return null
        }
        return userHandle!!.getByteArray(0, userHandleLength)
    }

    class ByValue: VerificationDataPOST(),  Structure.ByValue {
    }

    class ByReference: VerificationDataPOST,  Structure.ByReference {
        constructor(p: Pointer?) : super(p) {}
    }

}

@Structure.FieldOrder("origin", "userID", "userIDLength", "challenge", "rpId", "timeout")
open class VerificationDataGET: Structure {
    @JvmField var origin: String = ""
    @JvmField var userID: Pointer? = null
    @JvmField var userIDLength: Long = 0
    @JvmField var challenge: String = ""
    @JvmField var rpId: String = ""
    @JvmField var timeout: Int = 120000

    constructor() : super()

    override fun getFieldOrder(): List<String?>? {
        return listOf("origin", "userID", "userIDLength", "challenge", "rpId", "timeout")
    }

    class ByReference: VerificationDataGET(),  Structure.ByReference {}
}

@Structure.FieldOrder("origin", "challenge", "timeout", "rpId", "rpName", "userID", "accountName", "displayName")
open class RegistrationDataGET: Structure {
    @JvmField var origin: String = ""
    @JvmField var challenge: String = ""
    @JvmField var timeout: Int = 60000
    @JvmField var rpId: String = ""
    @JvmField var rpName: String = ""
    @JvmField var userID: String = ""
    @JvmField var accountName: String = ""
    @JvmField var displayName: String = ""

    constructor() : super()

    override fun getFieldOrder(): List<String?>? {
        return listOf("origin", "challenge", "timeout", "rpId", "rpName", "userID", "accountName", "displayName")
    }

    class ByReference: RegistrationDataGET(),  Structure.ByReference {}
}

@Structure.FieldOrder("attestationObjectBytes", "attestationObjectLength","credentialIdBytes","credentialIdLength", "origin", "type", "challenge","authenticationResult")
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
    constructor(p: Pointer?) : super(p) {
    }

    fun getAuthenticationResult(): AuthenticationResponse{
        return mapAuthenticationResponseENUM(authenticationResult)
    }

    override fun toString(): String {
        return "RegistrationDataPost(\n"+
        "   ${attestationObjectBytes}\n"+
        "   $attestationObjectLength\n"+
        "   $origin\n"+
        "   $type\n"+
        "   $challenge\n"+
        "   $authenticationResult\n" +
        ")"
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun getAttestationObjectBytes(): ByteArray? {

        if(attestationObjectBytes == null || attestationObjectLength <=0 || getAuthenticationResult()!= AuthenticationResponse.SUCCESS) {
            return null
        }
        val attstObj = attestationObjectBytes!!.getByteArray(0, attestationObjectLength)

        val cborFactory = CBORFactory()
        val jsonFactory = JsonFactory()
        val cborMapper = ObjectMapper( cborFactory)
        val jsonMapper = ObjectMapper(jsonFactory)  // Default JSON mapper for pretty print

        println(attstObj.toHexString())

        val node = cborMapper.readTree(attstObj)

        val prettyJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
        println("Output $prettyJson")

        return attstObj
    }

    fun getCredentialIDBytes(): ByteArray? {
        if(credentialIdBytes == null || credentialIdLength <=0) {
            return null
        }
        return credentialIdBytes!!.getByteArray(0, credentialIdLength)
    }

    class ByValue: RegistrationDataPOST(),  Structure.ByValue {
    }

    class ByReference: RegistrationDataPOST,  Structure.ByReference {
        constructor(p: Pointer?) : super(p) {}
    }

}
