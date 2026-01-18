/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.biometrics.windows

import com.sun.jna.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.mifos.authenticator.biometrics.mockServer.RegistrationDataGET
import org.mifos.authenticator.biometrics.mockServer.RegistrationDataPOST
import org.mifos.authenticator.biometrics.mockServer.RetrievedDataFromAuthenticator
import org.mifos.authenticator.biometrics.mockServer.VerificationDataGET
import org.mifos.authenticator.biometrics.mockServer.VerificationDataPOST
import org.mifos.authenticator.biometrics.mockServer.WindowsAuthenticationResponse
import org.mifos.authenticator.biometrics.mockServer.utils.generateBase64EncodedUID
import org.mifos.authenticator.biometrics.mockServer.utils.generateChallenge
import org.mifos.authenticator.biometrics.mockServer.utils.generateRandomUID

@Serializable
data class WindowsRegistrationResponse(
    val attestationObjectBytes: ByteArray,
    val credentialIdBytes: ByteArray,
    val credentialIdLength: Int,
    val userId: String,
    val windowsAuthenticationResponse: WindowsAuthenticationResponse,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowsRegistrationResponse

        if (credentialIdLength != other.credentialIdLength) return false
        if (!attestationObjectBytes.contentEquals(other.attestationObjectBytes)) return false
        if (!credentialIdBytes.contentEquals(other.credentialIdBytes)) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credentialIdLength
        result = 31 * result + attestationObjectBytes.contentHashCode()
        result = 31 * result + credentialIdBytes.contentHashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}

sealed class WindowsAuthenticatorResponse {
    sealed class Registration {
        class Success(val response: WindowsRegistrationResponse) : Registration()
        data class Error(val message: String) : Registration()
    }
    sealed class Verification {
        class Success(val response: WindowsAuthenticationResponse) : Verification()
        data object Error : Verification()
    }
}

class WindowsHelloAuthenticator(
    private val windowsHelloAuthenticator: WindowsHelloAuthenticatorNativeSupportImpl,
) {

    // Determines whether the platform authenticator service is available.
    fun checkIfWindowsHelloSupportedOrNot() = windowsHelloAuthenticator.checkIfAuthenticatorIsAvailable()

    suspend fun invokeUserRegistration(
        userId: String = "",
        accountName: String = "",
        displayName: String = "",
        timeout: Int = 0,
    ): WindowsAuthenticatorResponse.Registration {
        return withContext(Dispatchers.IO) {
            val challenge = generateChallenge()
            println(challenge)

            val registrationDataGET = RegistrationDataGET.ByReference()

            registrationDataGET.origin = "localhost"
            registrationDataGET.challenge = challenge
            registrationDataGET.timeout = if (timeout == 0) 120000 else timeout
            registrationDataGET.rpId = "localhost"
            registrationDataGET.rpName = "Mifos Initiative"
            registrationDataGET.userID = if (userId.isEmpty()) {
                generateRandomUID()
            } else {
                generateBase64EncodedUID(
                    userId,
                )
            }
            registrationDataGET.accountName = accountName.ifEmpty { "mifos@mifos.com" }
            registrationDataGET.displayName = displayName.ifEmpty { "MIFOS USER" }

            var registrationDataPOST: RegistrationDataPOST.ByValue? = null
            try {
                registrationDataPOST = windowsHelloAuthenticator.registerUser(registrationDataGET)

                val attestationObject = registrationDataPOST.getAttestationObjectBytes()
                val credentialIdBytes = registrationDataPOST.getCredentialIDBytes()

                if (attestationObject is RetrievedDataFromAuthenticator.Error) {
                    WindowsAuthenticatorResponse.Registration.Error(
                        attestationObject.message,
                    )
                } else if (credentialIdBytes is RetrievedDataFromAuthenticator.Error) {
                    WindowsAuthenticatorResponse.Registration.Error(
                        credentialIdBytes.message,
                    )
                } else {
                    val windowsRegistrationResponse = WindowsRegistrationResponse(
                        (attestationObject as RetrievedDataFromAuthenticator.Success).bytes,
                        (credentialIdBytes as RetrievedDataFromAuthenticator.Success).bytes,
                        credentialIdLength = registrationDataPOST.credentialIdLength,
                        userId = registrationDataGET.userID,
                        windowsAuthenticationResponse = registrationDataPOST.getAuthenticationResult(),
                    )
                    WindowsAuthenticatorResponse.Registration.Success(windowsRegistrationResponse)
                }
            } catch (e: Exception) {
                WindowsAuthenticatorResponse.Registration.Error(e.localizedMessage)
            } finally {
                registrationDataPOST?.let {
                    windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(
                        registrationData = RegistrationDataPOST.ByReference(it.pointer),
                    )
                }
                registrationDataPOST = null
            }
        }
    }

    suspend fun invokeUserVerification(
        windowsRegistrationResponse: WindowsRegistrationResponse,
        timeout: Int = 0,
    ): WindowsAuthenticatorResponse.Verification {
        return withContext(Dispatchers.IO) {
            val challenge = generateChallenge()

            println(challenge)

            val verificationDataGET = VerificationDataGET.ByReference()

            val nativeCredID = Memory(windowsRegistrationResponse.credentialIdBytes.size.toLong())

            nativeCredID.write(
                0,
                windowsRegistrationResponse.credentialIdBytes,
                0,
                windowsRegistrationResponse.credentialIdBytes.size,
            )

            verificationDataGET.origin = "localhost"
            verificationDataGET.challenge = challenge
            verificationDataGET.userID = nativeCredID
            verificationDataGET.userIDLength = windowsRegistrationResponse.credentialIdBytes.size.toLong()
            verificationDataGET.rpId = "localhost"
            verificationDataGET.timeout = if (timeout == 0) 120000 else timeout

            var verificationDataPOST: VerificationDataPOST.ByValue? = null
            try {
                verificationDataPOST = windowsHelloAuthenticator.verifyUser(verificationDataGET)

                val verificationResponse = verificationDataPOST.getVerificationResult()
                WindowsAuthenticatorResponse.Verification.Success(verificationResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                WindowsAuthenticatorResponse.Verification.Error
            } finally {
                verificationDataPOST?.let {
                    windowsHelloAuthenticator.FreeVerificationDataPOSTContents(
                        verificationDataPOST = VerificationDataPOST.ByReference(it.pointer),
                    )
                }
                verificationDataPOST = null
                nativeCredID.close()
            }
        }
    }
}
