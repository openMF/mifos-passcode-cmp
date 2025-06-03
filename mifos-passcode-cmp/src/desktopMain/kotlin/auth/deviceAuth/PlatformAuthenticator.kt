package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.application
import com.mifos.passcode.mock_server.models.UserAuthentication
import com.mifos.passcode.mock_server.models.UserRegistration
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.generateRandomUID
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

const val CREDENTIAL_RECORD_KEY = "CREDENTIAL_RECORDS"

actual class PlatformAuthenticator private actual constructor(){

    actual constructor(activity: Any?) : this()

    private var scope = CoroutineScope(Dispatchers.Default)

    val userRegistration by lazy {
        UserRegistration()
    }

    val userVerification by lazy {
        UserAuthentication()
    }

    val windowsHelloAuthenticator by lazy {
        WindowsHelloAuthenticatorImpl()
    }

    val dataBase by lazy {
        DataBase()
    }

    val isWindowsTenOrHigh = if(Platform.isWindows()) isWindowsTenOrEleven()  else false


    actual fun getDeviceAuthenticatorStatus(): AuthenticatorStatus {

        if(isWindowsTenOrHigh){
            if(windowsHelloAuthenticator.checkIfAuthenticatorIsAvailable()){
                return AuthenticatorStatus(
                    userCredentialSet = true,
                    biometricsNotPossible = false,
                    biometricsSet = true,
                    message = "It's windows"
                )
            }
        }
        return AuthenticatorStatus(
            userCredentialSet = false,
            biometricsNotPossible = true,
            biometricsSet = false,
            message = "Coming Soon"
        )
    }

    actual fun setDeviceAuthOption() {}


    actual suspend fun registerUser(): AuthenticationResult{
        lateinit var registrationData: RegistrationDataPOST.ByValue

        if(isWindowsTenOrHigh){

            println("Entered the if statement")

            val challenge = generateChallenge()
//            val userID = generateRandomUID()

            println(challenge)

            val verificationResponse: MutableState<Pair<Boolean, WindowsAuthenticatorData?>> = mutableStateOf(Pair(false, null))

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
                    registrationData = windowsHelloAuthenticator.registerUser(registrationDataGET)
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
                println(registrationData)
                val verificationJob = scope.async(Dispatchers.Unconfined) {
                    verificationResponse.value = userRegistration.verifyRegistrationResponse(
                        attestationObjectBytes = registrationData.getAttestationObjectBytes(),
                        userId = registrationDataGET.userID,
                        challenge = registrationDataGET.challenge
                    )
                }
                verificationJob.await()

                if(verificationJob.isCompleted){
                    if(verificationResponse.value.first){
                            println("Entering data saving block.")
                        try {
                            dataBase.saveCredentialRecord(verificationResponse.value.second!!)
                            println("Data saved successfully")
                        }catch (e: Exception){
                            println(e.message)
                            e.printStackTrace()
                            println("Error while saving data.")
                        }

                        println("Registration successful")
                        windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationData.pointer))
                        return AuthenticationResult.Success("Registration Successful")
                    }
                    println("Registration failed")
                    windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationData.pointer))
                    return AuthenticationResult.Error("Registration failed")
                }
            }
        }

        if(isWindowsTenOrHigh){
            windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationData.pointer))

            println("Error during registration.")
        }
        return AuthenticationResult.Error("Error")
    }

    @OptIn(ExperimentalStdlibApi::class)
    actual suspend fun authenticate(title: String): AuthenticationResult {
        lateinit var verificationData: VerificationDataPOST.ByValue

        if(isWindowsTenOrHigh){
            println("Entered the if statement")

            val challenge = generateChallenge()

            println(challenge)

            val verificationResponse: MutableState<Pair<Boolean, WindowsAuthenticatorData?>> = mutableStateOf(Pair(false, null))

            val verificationDataGET = VerificationDataGET.ByReference()

            val windowsAuthenticatorData = dataBase.getCredentialRecord()

            verificationDataGET.origin = "localhost"
            verificationDataGET.challenge = challenge
            verificationDataGET.userID = windowsAuthenticatorData.userId
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
                    verificationData = windowsHelloAuthenticator.verifyUser(verificationDataGET)
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
                println(verificationData.getUserHandleBytes()!!.toHexString())
                println(verificationData.getAuthenticatorDataBytes()!!.toHexString())
                println(verificationData.getSignatureDataBytes()!!.toHexString())

                val verificationJob = scope.async(Dispatchers.Unconfined) {
                    verificationResponse.value = userVerification.verifyAuthenticationResponse(
                        windowsAuthenticatorData,
                        verificationData.getUserHandleBytes()!!,
                        verificationData.getAuthenticatorDataBytes()!!,
                        verificationData.getSignatureDataBytes()!!,
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
                        windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationData.pointer),)
                        return AuthenticationResult.Success("Verification Successful")
                    }
                    println("Verification failed")
                    return AuthenticationResult.Error("Registration failed")
                }
            }

        }
        if(isWindowsTenOrHigh){
            windowsHelloAuthenticator.FreeVerificationDataPOSTContents(verificationDataPOST = VerificationDataPOST.ByReference(verificationData.pointer),)

            println("Error during verification response verification.")
        }
        return AuthenticationResult.Error("Coming Soon")
    }

}


interface WindowsHelloAuthenticator: Library  {

    fun checkIfAuthenticatorIsAvailable(): Boolean

    fun verifyUser(verificationData: VerificationDataGET.ByReference): VerificationDataPOST.ByValue

    fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue

    fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference)

    fun FreeVerificationDataPOSTContents(verificationDataPOST: VerificationDataPOST.ByReference)
}

class WindowsHelloAuthenticatorImpl: WindowsHelloAuthenticator{

    private val native by lazy {
        Native.load("WindowsHelloAuthenticator", WindowsHelloAuthenticator::class.java)
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

fun isWindowsTenOrEleven(): Boolean{

    val rt= Runtime.getRuntime()
    val process = rt.exec("SYSTEMINFO")

    val readOutput = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?

    while (true){
        line = readOutput.readLine()
        if(line==null) break;
        if(
            ( line.contains("Windows 10") ||
                    line.contains("Windows 11") )
        ){
            return true
        }
    }

    return false
}

class DataBase(){
    val dataStore by lazy {
        Settings()
    }


    fun saveCredentialRecord(windowsAuthenticatorData: WindowsAuthenticatorData){

        val stringifiedCredRecords = Json.encodeToString(windowsAuthenticatorData)
        dataStore.putString(CREDENTIAL_RECORD_KEY, stringifiedCredRecords)
    }

    fun getCredentialRecord(): WindowsAuthenticatorData{
        val fetchedData = dataStore[CREDENTIAL_RECORD_KEY, ""]

        return Json.decodeFromString(fetchedData)
    }

    fun removeCredRecords(){
        dataStore.remove(CREDENTIAL_RECORD_KEY)
    }
}

@Serializable
data class WindowsAuthenticatorData(
    val attestationObjectBytes: ByteArray,
    val collectedClientDataBytes: ByteArray,
    val credentialIdBytes: ByteArray,
    val userId: String,
    val oldChallenge: String,
    val counter: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowsAuthenticatorData

        if (!attestationObjectBytes.contentEquals(other.attestationObjectBytes)) return false
        if (!collectedClientDataBytes.contentEquals(other.collectedClientDataBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attestationObjectBytes.contentHashCode()
        result = 31 * result + collectedClientDataBytes.contentHashCode()
        return result
    }
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
fun main() = application {

    val platformAuthenticator = PlatformAuthenticator("1000")

    runBlocking {
        val database = DataBase()
//        async {
//            database.removeCredRecords()
//        }.await()
//
//        val x =async {
//            platformAuthenticator.registerUser()
//        }
//        x.await()
//
//        async {
//            println(database.getCredentialRecord())
//        }.await()
//
//        println(x.getCompleted())
////
////        delay(2000)
//
//        println("\n\n\n\n\n\n Starting verification process: \n\n\n\n")
        val y =async {
            platformAuthenticator.authenticate()
        }
        y.await()

        async {
            println(database.getCredentialRecord())
        }.await()

        println(y.getCompleted())
    }

}