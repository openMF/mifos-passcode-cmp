// package com.mifos.passcode.mock_server.models

/**
 * The Windows implementation is a workaround. This is similar to what is used on websites.
 * On websites, when you create an account, you sometimes also create a Passkey as a 2FA option.
 * You use that Passkey for signing in again when you are logged out or for any other authentication
 * on the website related to your account.
 *
 * So, in the process, what is actually happening is that the server creates a JSON file and sends it to the
 * client (frontend).
 * The frontend parses this JSON and sends it to the Platform Authenticator for registering the user.
 * If the user registers successfully with the authenticator, it sends data to the client, which the client
 * sends to the server.
 *
 * Now here is what these two classes do: The server, in each case of registration and verification,
 * verifies the data sent by the authenticator and then, depending on the result, either creates the passkey
 * or not.
 *
 * Why have I commented them out?
 * The UserRegistration class is working, but this one is not as expected.
 * Since the Windows implementation is completely offline, there is no
 * data manipulation happening in between. So I am just checking the result
 * sent by the Windows Hello authenticator.
 *
 * Why am I keeping this?
 * For web implementation, these will be required.
 * I will modify both classes as per need.
 */

// import com.mifos.passcode.auth.deviceAuth.WindowsAuthenticatorData
// import com.mifos.passcode.mock_server.utils.getAttestationObject
// import com.mifos.passcode.mock_server.utils.getAuthenticatorDataAuthenticationExtensionAuthenticatorOutput
// import com.mifos.passcode.mock_server.utils.getCollectdClientDataBytes
// import com.webauthn4j.WebAuthnManager
// import com.webauthn4j.authenticator.AuthenticatorImpl
// import com.webauthn4j.converter.exception.DataConversionException
// import com.webauthn4j.credential.CredentialRecord
// import com.webauthn4j.data.AuthenticationData
// import com.webauthn4j.data.AuthenticationParameters
// import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData
// import com.webauthn4j.data.client.ClientDataType
// import com.webauthn4j.data.client.CollectedClientData
// import com.webauthn4j.data.client.Origin
// import com.webauthn4j.data.client.challenge.DefaultChallenge
// import com.webauthn4j.server.ServerProperty
// import com.webauthn4j.verifier.exception.VerificationException
//
//
// //class UserAuthentication(){
// //
// //    private val webAuthManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()
// //
// //    @OptIn(ExperimentalStdlibApi::class)
// //    @Throws(DataConversionException::class)
// //    suspend fun verifyAuthenticationResponse(
// //        windowsAuthenticatorData: WindowsAuthenticatorData,
// //        userHandle: ByteArray,
// //        authenticatorDataBytes: ByteArray,
// //        signature: ByteArray,
// //        origin: String = "localhost",
// //        rpId: String = "localhost",
// //        type: ClientDataType = ClientDataType.WEBAUTHN_GET,
// //        challenge: String,
// //    ): Pair<Boolean, WindowsAuthenticatorData?>{
// //
// //        lateinit var authenticationData: AuthenticationData
// //
// //        val mOrigin = Origin(origin)
// //        val mChallenge = DefaultChallenge(challenge)
// //
// //        val actualCollectedClientDataBytes = getCollectdClientDataBytes(origin, type.value, challenge)
// //
// //        val attestationObject =  getAttestationObject(windowsAuthenticatorData.attestationObjectBytes)
// //
// //
// //        println("AttestationObject from inside verifyAuthenticationResponse: $attestationObject")
// //
// //
// //
// //        val collectedClientData = CollectedClientData(
// //            type,
// //            mChallenge,
// //            mOrigin,
// //            false,
// //            null
// //        )
// //
// //        try {
// ////            authenticationData = webAuthManager.parseAuthenticationResponseJSON(authenticationResponse)
// //            authenticationData = AuthenticationData(
// //                windowsAuthenticatorData.credentialIdBytes,
// //                userHandle,
// //                getAuthenticatorDataAuthenticationExtensionAuthenticatorOutput(authenticatorDataBytes),
// //                authenticatorDataBytes,
// //                collectedClientData,
// //                actualCollectedClientDataBytes,
// //                null,
// //                signature
// //            )
// //            println("Signature data used for verification: ${authenticationData.signature}")
// //            println(authenticationData.toString())
// //        } catch (e: DataConversionException){
// //            println(e.message)
// //            throw e
// //        }
// //
// //        val serverProperty = ServerProperty(
// //            mOrigin,
// //            rpId,
// //            mChallenge
// //        )
// //
// //        val allowCredentials: MutableList<ByteArray?>? = mutableListOf(windowsAuthenticatorData.credentialIdBytes)
// //        val userVerificationRequired = true
// //        val userPresenceRequired = true
// //
// //
// //
// //        val authenticator = AuthenticatorImpl(
// //            attestationObject?.authenticatorData?.attestedCredentialData!!,
// //            attestationObject.attestationStatement,
// //            0
// //        )
// //
// //        val credRecord = object:  CredentialRecord{
// //            override fun getClientData(): CollectedClientData? {
// //                return collectedClientData
// //            }
// //
// //            override fun isUvInitialized(): Boolean? {
// //                return attestationObject.authenticatorData.isFlagUV
// //            }
// //
// //            override fun setUvInitialized(value: Boolean) {
// //
// //            }
// //
// //            override fun isBackupEligible(): Boolean? {
// //                return false
// //            }
// //
// //            override fun setBackupEligible(value: Boolean) {
// //
// //            }
// //
// //            override fun isBackedUp(): Boolean? {
// //                return false
// //            }
// //
// //            override fun setBackedUp(value: Boolean) {
// //            }
// //
// //            override fun getAttestedCredentialData(): AttestedCredentialData {
// //                return authenticationData.authenticatorData!!.attestedCredentialData!!
// //            }
// //
// //            override fun getCounter(): Long {
// //                return authenticationData.authenticatorData!!.signCount
// //            }
// //
// //            override fun setCounter(value: Long) {
// //            }
// //
// //        }
// //
// //        println(credRecord)
// //
// //        println("Credential record for verification: ")
// //        println("Attested credential data: ${authenticator.attestedCredentialData}")
// //        println("cose key: ${authenticator.attestedCredentialData.coseKey.toString()}")
// //        println("public key format: ${authenticator.attestedCredentialData.coseKey.publicKey?.format}")
// //        println("public key encoded: ${authenticator.attestedCredentialData.coseKey.publicKey?.encoded?.toHexString()}")
// //        println("credential id: ${authenticator.attestedCredentialData.credentialId.toHexString()}")
// //
// //        val authenticationParameters = AuthenticationParameters(
// //            serverProperty,
// //            credRecord,
// //            allowCredentials,
// //            userVerificationRequired,
// //            userPresenceRequired,
// //        )
// //
// //        val windowsAuthenticatorDataToSave =windowsAuthenticatorData.copy(
// //            counter = windowsAuthenticatorData.counter + 1,
// //        )
// //
// //        println(windowsAuthenticatorDataToSave)
// //
// //        try {
// //            authenticationData = webAuthManager.verify(authenticationData, authenticationParameters)
// //            return Pair(true, windowsAuthenticatorDataToSave)
// //        }catch (e: VerificationException){
// //            e.printStackTrace()
// //            println("Personalized message: ")
// //            println(e.message)
// //            println(e.cause)
// //            println(e.localizedMessage)
// //
// //            return Pair(false, null)
// //        }
// //
// //    }
// //
// //}
