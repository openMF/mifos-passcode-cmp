package com.mifos.passcode.auth.deviceAuth

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.sun.jna.Pointer
import com.sun.jna.Structure


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
    @JvmField var authenticationResult: Boolean = false

    constructor() : super()
    constructor(p: Pointer?) : super(p) {}

    fun getAuthenticatorDataBytes(): ByteArray? {
        if (authenticatorDataBytes == null || authenticatorDataLength <= 0) {
            return null
        }
        return authenticatorDataBytes!!.getByteArray(0, authenticatorDataLength)
    }

    fun getSignatureDataBytes(): ByteArray? {
        if (signatureDataBytes == null || signatureDataBytesLength <= 0) {
            return signatureDataBytes!!.getByteArray(0, signatureDataBytesLength)
        }
        return null
    }

    fun getUserHandleBytes(): ByteArray? {
        if (userHandle == null || userHandleLength <= 0) {
            return userHandle!!.getByteArray(0, userHandleLength)
        }
        return null
    }

    class ByValue: VerificationDataPOST(),  Structure.ByValue {
    }

    class ByReference: VerificationDataPOST,  Structure.ByReference {
        constructor(p: Pointer?) : super(p) {}
    }

}

@Structure.FieldOrder("origin", "userID", "challenge", "rpId", "timeout")
open class VerificationDataGET: Structure {
    @JvmField var origin: String = ""
    @JvmField var userID: String = ""
    @JvmField var challenge: String = ""
    @JvmField var rpId: String = ""
    @JvmField var timeout: Int = 120000

    constructor() : super()

    override fun getFieldOrder(): List<String?>? {
        return listOf("origin", "userID", "challenge", "rpId", "timeout")
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

@Structure.FieldOrder("attestationObjectBytes", "attestationObjectLength","origin", "type", "challenge","authenticationResult")
open class RegistrationDataPOST : Structure {
    @JvmField var attestationObjectBytes: Pointer? = null
    @JvmField var attestationObjectLength: Int = 0
    @JvmField var origin: String = ""
    @JvmField var type: String = "webauthn.create"
    @JvmField var challenge: String = ""
    @JvmField var authenticationResult: Boolean = false

    constructor() : super()
    constructor(p: Pointer?) : super(p) {
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
    fun getAttestationObjectBytes(): ByteArray {
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


    class ByValue: RegistrationDataPOST(),  Structure.ByValue {
    }

    class ByReference: RegistrationDataPOST,  Structure.ByReference {
        constructor(p: Pointer?) : super(p) {}
    }

}
