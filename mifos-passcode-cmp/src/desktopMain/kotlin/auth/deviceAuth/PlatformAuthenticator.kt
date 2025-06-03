package com.mifos.passcode.auth.deviceAuth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.application
import com.mifos.passcode.mock_server.models.UserRegistration
import com.mifos.passcode.mock_server.utils.generateChallenge
import com.mifos.passcode.mock_server.utils.generateRandomUID
import com.mifos.passcode.mock_server.utils.getCollectdClientDataBytes
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Platform
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.client.ClientDataType
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

    val windowsHelloAuthenticator by lazy {
        WindowsHelloAuthenticatorImpl()
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
            val userID = generateRandomUID()

            println(challenge)

            val dataBase = DataBase()

            val verificationResponse: MutableState<Pair<Boolean, CredentialRecordImpl?>> = mutableStateOf(Pair(false, null))

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
                println("Entering verification block")
                println(registrationData)
                val verificationJob = scope.async(Dispatchers.Unconfined) {
                    verificationResponse.value = userRegistration.verifyRegistrationResponse(
                        attestationObjectBytes = registrationData.getAttestationObjectBytes(),
                        challenge = challenge
                    )
                }
                verificationJob.await()

                if(verificationJob.isCompleted){
                    if(verificationResponse.value.first){
                        println("Entering data saving block.")


                        try {
                            val windowsAuthenticatorData = WindowsAuthenticatorData(
                                registrationData.getAttestationObjectBytes(),
                                collectedClientDataBytes = getCollectdClientDataBytes(
                                    origin = registrationDataGET.origin,
                                    challenge = registrationDataGET.challenge,
                                    type = ClientDataType.WEBAUTHN_CREATE.value
                                ),
                            )
                            dataBase.saveCredentialRecord(windowsAuthenticatorData)
                            println("Data saved successfully")
                        }catch (e: Exception){
                            println(e.message)
                            e.printStackTrace()
                            println("Error while saving data.")
                        }

                        println("Verification successful")
                        windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationData.pointer))
                        return AuthenticationResult.Success("Registration Successful")
                    }
                    println("Verification failed")
                    return AuthenticationResult.Error("Registration failed")
                }
            }

        }

        windowsHelloAuthenticator.FreeRegistrationDataPOSTContents(registrationData = RegistrationDataPOST.ByReference(registrationData.pointer))

        println("Error during registration.")
        return AuthenticationResult.Error("Error")
    }

    actual suspend fun authenticate(title: String): AuthenticationResult {
        return AuthenticationResult.Error("Coming Soon")
    }

}


interface WindowsHelloAuthenticator: Library  {

    fun checkIfAuthenticatorIsAvailable(): Boolean

    fun verifyUser(verificationData: VerificationDataGET): VerificationDataPOST

    fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue

    fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference)

}

class WindowsHelloAuthenticatorImpl: WindowsHelloAuthenticator{

    private val native by lazy {
        Native.load("WindowsHelloAuthenticator", WindowsHelloAuthenticator::class.java)
    }

    override fun checkIfAuthenticatorIsAvailable(): Boolean {
        return native.checkIfAuthenticatorIsAvailable()
    }

    override fun verifyUser(verificationData: VerificationDataGET): VerificationDataPOST {
        return native.verifyUser(verificationData)
    }

    override fun registerUser(registrationData: RegistrationDataGET.ByReference): RegistrationDataPOST.ByValue {
        return native.registerUser(registrationData)
    }

    override fun FreeRegistrationDataPOSTContents(registrationData: RegistrationDataPOST.ByReference) {
        return native.FreeRegistrationDataPOSTContents(registrationData)
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

@OptIn(DelicateCoroutinesApi::class)
fun main() = application{

    val platformAuthenticator = PlatformAuthenticator("1000")

    val database = DataBase()

    runBlocking {
        val x =async {
            platformAuthenticator.registerUser()
        }
        x.await()
        async {
            println(database.getCredentialRecord())
        }.await()

        println(x)
    }

}