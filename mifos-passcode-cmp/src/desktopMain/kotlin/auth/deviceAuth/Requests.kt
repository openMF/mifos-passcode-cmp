package com.mifos.passcode.auth.deviceAuth

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.sun.jna.Pointer
import com.sun.jna.Structure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmField


@Serializable
data class AttestationObjectCBOR(
    @SerialName("fmt") val fmt: String,
    @SerialName("attStmt") val attStmt: ByteArray,
    @SerialName("authData") val authData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttestationObjectCBOR

        if (fmt != other.fmt) return false
        if (!attStmt.contentEquals(other.attStmt)) return false
        if (!authData.contentEquals(other.authData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fmt.hashCode()
        result = 31 * result + attStmt.contentHashCode()
        result = 31 * result + authData.contentHashCode()
        return result
    }
}


@Structure.FieldOrder("authenticatorDataBytes", "signatureDataBytes","signatureDataBytesLength" ,"authenticatorDataLength", "origin", "challenge", "type", "authenticationResult")
open class VerificationDataPOST: Structure.ByValue{
    @JvmField var authenticatorDataBytes: Pointer? = null
    @JvmField var signatureDataBytes: Pointer? = null
    @JvmField var signatureDataBytesLength: Int = 0
    @JvmField var authenticatorDataLength: Int = 0
    @JvmField var userHandle: Pointer? = null
    @JvmField var origin: String = ""
    @JvmField var challenge: String = ""
    @JvmField var type: String = ""
    @JvmField var authenticationResult: Boolean = false

    fun getAuthenticatorData(): ByteArray? {
        if (authenticatorDataBytes == null || authenticatorDataLength <= 0) {
            return null
        }
        return authenticatorDataBytes!!.getByteArray(0, authenticatorDataLength)
    }

    fun getSignatureData(): ByteArray? {
        if (signatureDataBytes == null || signatureDataBytesLength <= 0) {
            return signatureDataBytes!!.getByteArray(0, signatureDataBytesLength)
        }
        return null
    }

    constructor() : super()

}

@Structure.FieldOrder("origin", "userID", "challenge", "rpId", "timeout")
open class VerificationDataGET: Structure.ByReference {
    @JvmField var origin: String = ""
    @JvmField var userID: String = ""
    @JvmField var challenge: String = ""
    @JvmField var rpId: String = ""
    @JvmField var timeout: Int = 120000

    constructor() : super()
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
