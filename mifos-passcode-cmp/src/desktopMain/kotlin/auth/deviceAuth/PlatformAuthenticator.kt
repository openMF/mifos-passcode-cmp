package com.mifos.passcode.auth.deviceAuth

import com.mifos.passcode.mock_server.models.RegistrationResponse
import com.mifos.passcode.mock_server.models.WindowsAuthenticatorResponse
import com.mifos.passcode.mock_server.models.WindowsHelloAuthenticator
import com.mifos.passcode.mock_server.utils.isWindowsTenOrEleven
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


const val CREDENTIAL_RECORD_KEY = "CREDENTIAL_RECORDS"

actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()

    private var scope = CoroutineScope(Dispatchers.Default)

    private val isWindowsTenOrHigh = if(Platform.isWindows()) isWindowsTenOrEleven()  else false

    private val windowsHelloAuthenticatorNativeSupport by lazy {
        WindowsHelloAuthenticatorNativeSupportImpl()
    }

    private val windowsHelloAuthenticator by lazy {
        WindowsHelloAuthenticator(windowsHelloAuthenticatorNativeSupport)
    }

    actual fun getDeviceAuthenticatorStatus(): PlatformAuthenticatorStatus {
        if(isWindowsTenOrHigh){
            return PlatformAuthenticatorStatus.DesktopAuthenticatorStatus.WindowsAuthenticatorStatus(windowsHelloAuthenticator.checkIfWindowsHelloSupportedOrNot())
        }
        return PlatformAuthenticatorStatus.UnsupportedPlatform()
    }


    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(): AuthenticationResult {
        if(isWindowsTenOrHigh){
            lateinit var windowsAuthResponse: WindowsAuthenticatorResponse.Registration

            scope.async {
                windowsAuthResponse = windowsHelloAuthenticator.invokeUserRegistration()
            }.await()

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Registration.Error){
                return AuthenticationResult.Error("Error while registering user")
            }
            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Registration.Success).response
            if(response.authenticationResponse == AuthenticationResponse.SUCCESS){
                val registrationData = (windowsAuthResponse as WindowsAuthenticatorResponse.Registration.Success).response

                return AuthenticationResult.Success(encodeWindowsAuthenticatorToJsonString(registrationData))
            }
            return AuthenticationResult.Failed(response.authenticationResponse.toString())
        }
        return AuthenticationResult.Error("Coming Soon")
    }


    @OptIn(ExperimentalStdlibApi::class)
    actual suspend fun authenticate(title: String, savedRegistrationOutput: String): AuthenticationResult {

        if(isWindowsTenOrHigh){
            val registrationResponse: RegistrationResponse? =
                decodeWindowsAuthenticatorFromJson(savedRegistrationOutput) ?: return AuthenticationResult.Error("Invalid registration data")

            lateinit var windowsAuthResponse: WindowsAuthenticatorResponse.Verification

            scope.async {
                windowsAuthResponse = windowsHelloAuthenticator.invokeUserVerification(registrationResponse!!)
            }.await()

            if(windowsAuthResponse is WindowsAuthenticatorResponse.Verification.Error){
                return AuthenticationResult.Error("Error while verifying user user")
            }
            val response = (windowsAuthResponse as WindowsAuthenticatorResponse.Verification.Success).response
            if(response == AuthenticationResponse.SUCCESS) return AuthenticationResult.Success(response.toString())

            return AuthenticationResult.Failed(response.toString())
        }

        return AuthenticationResult.Error("Coming Soon")
    }

}


private interface WindowsHelloAuthenticatorNativeSupport: Library  {

    fun checkIfAuthenticatorIsAvailable(): Boolean

    fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue

    fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue

    fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference)

    fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference)
}

final class WindowsHelloAuthenticatorNativeSupportImpl: WindowsHelloAuthenticatorNativeSupport{

    private val native by lazy {
        Native.load("WindowsHelloAuthenticator", WindowsHelloAuthenticatorNativeSupport::class.java)
    }

    override fun checkIfAuthenticatorIsAvailable(): Boolean {
        return native.checkIfAuthenticatorIsAvailable()
    }

    override fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue {
        return native.verifyUser(verificationData)
    }

    override fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue {
        return native.registerUser(registrationData)
    }

    override fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference) {
        return native.FreeRegistrationDataPOSTContents(registrationData)
    }

    override fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference) {
        return native.FreeVerificationDataPOSTContents(verificationDataPOST)
    }
}

private fun encodeWindowsAuthenticatorToJsonString(registrationResponse: RegistrationResponse): String {
    return Json.encodeToString(registrationResponse)
}

private fun decodeWindowsAuthenticatorFromJson(jsonString: String): RegistrationResponse? {
    return Json.decodeFromString(jsonString)
}

//@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
//fun main() = application {
//
//    val platformAuthenticator = PlatformAuthenticator("1000")
//
//    runBlocking {
//        val database = WindowsAuthenticatorDataBase()
//        async {
//            database.removeRegistrationResponse()
//        }.await()
//
//        val x =async {
//            platformAuthenticator.registerUser()
//        }
//        x.await()
//
//        async {
//            println("Registration data saved: ")
//            println(database.getRegistrationResponse())
//        }.await()
//
//        println(x.getCompleted())
//
//        delay(2000)
//
//        println("\n\n\n\n\n\n Starting verification process: \n\n\n\n")
//        val y =async {
//            platformAuthenticator.authenticate()
//        }
//        y.await()
//
//        println(y.getCompleted())
//    }
//
//}



/*
    actual suspend fun registerUser(): AuthenticationResult{
        lateinit var registrationDataPOST: RegistrationDataPOST.ByValue

        if(isWindowsTenOrHigh){

            println("Entered the if statement")

            val challenge = generateChallenge()
//            val userID = generateRandomUID()

            println(challenge)

            val registrationVerificationResponse: MutableState<Pair<Boolean, WindowsAuthenticatorData?>> = mutableStateOf(Pair(false, null))

            val registrationDataGET = RegistrationDataGET.ByReference()

            registrationDataGET.origin = "localhost"
            registrationDataGET.challenge = challenge
            registrationDataGET.timeout = 120000
            registrationDataGET.rpId = "localhost"
            registrationDataGET.rpName = "MIFOS"
            registrationDataGET.userID = "yFDHoMO7pvCbKS9wrn-MHw"
            registrationDataGET.accountName = "mifos@mifos.com"
            registrationDataGET.displayName = "MIFOS USER"

            val registrationJob = scope.async(Dispatchers.IO) {
                println("Entered first async block")

                try {
                    println("Initiating registration.")
                    registrationDataPOST = windowsHelloAuthenticator.registerUser(registrationDataGET)
                }catch (e: Exception){
                    e.printStackTrace()
                    return@async AuthenticationResult.Error("Registration failed")
                }finally {
                    println("Exiting the registration block")
                }
            }
            registrationJob.await()


            if(registrationJob.isCompleted){
                println("Entering registration response verification block.")
                println(registrationDataPOST)
                val verificationJob = scope.async(Dispatchers.Unconfined) {
                    registrationVerificationResponse.value = userRegistration.verifyRegistrationResponse(
                        attestationObjectBytes = registrationDataPOST.getAttestationObjectBytes()!!,
                        credentialIdBytes = registrationDataPOST.getCredentialIDBytes()!!,
                        credentialIdLength = registrationDataPOST.credentialIdLength,
                        userId = registrationDataGET.userID,
                        challenge = registrationDataGET.challenge
                    )
                }
                verificationJob.await()

                if(verificationJob.isCompleted){
                    if(registrationVerificationResponse.value.first){
                            println("Entering data saving block.")
                        try {
                            dataBase.saveCredentialRecord(registrationVerificationResponse.value.second!!)
                            println("Data saved successfully")
                        }catch (e: Exception){
                            println(e.message)
                            e.printStackTrace()
                            println("Error while saving data.")
                        }

                        println("Registration successful")
                        windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationDataPOST.pointer))
                        return AuthenticationResult.Success("Registration Successful")
                    }
                    println("Registration failed")
                    windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationDataPOST.pointer))
                    return AuthenticationResult.Error("Registration failed")
                }
            }
        }

        if(isWindowsTenOrHigh){
            windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationDataPOST.pointer))

            println("Error during registration.")
        }
        return AuthenticationResult.Error("Error")
    }
*/


/*
        if(isWindowsTenOrHigh){
            println("Entered the if statement")

            val challenge = generateChallenge()

            println(challenge)

            val verificationResponse: MutableState<Pair<Boolean, WindowsAuthenticatorData?>> = mutableStateOf(Pair(false, null))

            val verificationDataGET = VerificationDataGET.ByReference()

            var windowsAuthenticatorData = dataBase.getCredentialRecord()
            println("Data loaded by authentication function1: $windowsAuthenticatorData")

            val nativeCredID = Memory(windowsAuthenticatorData.credentialIdBytes.size.toLong())
            nativeCredID.write(0, windowsAuthenticatorData.credentialIdBytes,0,windowsAuthenticatorData.credentialIdBytes.size)

            verificationDataGET.origin = "localhost"
            verificationDataGET.challenge = challenge
            verificationDataGET.userID = nativeCredID
            verificationDataGET.userIDLength = windowsAuthenticatorData.credentialIdBytes.size.toLong()
            verificationDataGET.rpId = "localhost"
            verificationDataGET.timeout = 120000

            println("Data sent from Kotlin")
            println("Origin: ${verificationDataGET.origin}")
            println("Challenge: ${verificationDataGET.challenge}")
            println("userID: ${verificationDataGET.userID}")
            println("rpId: ${verificationDataGET.rpId}")

            val userVerificationJob = scope.async(Dispatchers.IO) {
                println("Entered first async block for user verification")

                try {
                    println("Initiating verification response verification.")
                    verificationDataPOST = windowsHelloAuthenticatorNative.verifyUser(verificationDataGET)
                    println("Verification successful")
                }catch (e: Exception){
                    e.printStackTrace()
                    return@async AuthenticationResult.Error("User verification failed")
                }finally {
                    println("Exiting the verification block")
                }
            }
            userVerificationJob.await()

            if(userVerificationJob.isCompleted){
                println("Entering verification response verification block.")
                println("Data loaded by authentication function2: $windowsAuthenticatorData")

                val verificationJob = scope.async(Dispatchers.Unconfined) {
                    verificationResponse.value = userVerification.verifyAuthenticationResponse(
                        windowsAuthenticatorData,
                        verificationDataPOST.getUserHandleBytes()!!,
                        verificationDataPOST.getAuthenticatorDataBytes()!!,
                        verificationDataPOST.getSignatureDataBytes()!!,
                        challenge = challenge
                    )
                }
                verificationJob.await()

                if(verificationJob.isCompleted){
                    if(verificationResponse.value.first){
                        println("Entering data saving block.")

                        try {
                            dataBase.saveCredentialRecord(verificationResponse.value.second!!)
                        }catch (e: Exception){
                            println(e.message)
                            e.printStackTrace()
                            println("Error while saving data.")
                        }
                        println("Verification successful")
                        nativeCredID.clear()
                        windowsHelloAuthenticatorNative.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationDataPOST.pointer),)
                        return AuthenticationResult.Success("Verification Successful")
                    }
                    nativeCredID.clear()
                    println("Verification failed")
                    return AuthenticationResult.Error("Verification failed")
                }
            }
            nativeCredID.clear()
        }

 */