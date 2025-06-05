package auth.deviceAuth.windowsComponents

import kotlinx.serialization.Serializable


@Serializable
data class RegistrationResponse(
    val attestationObjectBytes: ByteArray,
    val collectedClientDataBytes: ByteArray,
    val credentialIdBytes: ByteArray,
    val credentialIdLength: Int,
    val userId: String,
    val oldChallenge: String,
    val counter: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RegistrationResponse

        if (credentialIdLength != other.credentialIdLength) return false
        if (counter != other.counter) return false
        if (!attestationObjectBytes.contentEquals(other.attestationObjectBytes)) return false
        if (!collectedClientDataBytes.contentEquals(other.collectedClientDataBytes)) return false
        if (!credentialIdBytes.contentEquals(other.credentialIdBytes)) return false
        if (userId != other.userId) return false
        if (oldChallenge != other.oldChallenge) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credentialIdLength
        result = 31 * result + counter.hashCode()
        result = 31 * result + attestationObjectBytes.contentHashCode()
        result = 31 * result + collectedClientDataBytes.contentHashCode()
        result = 31 * result + credentialIdBytes.contentHashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + oldChallenge.hashCode()
        return result
    }

}