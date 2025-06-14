package com.mifos.passcode.auth.deviceAuth.windows.utils

import com.mifos.passcode.auth.deviceAuth.windows.WindowsRegistrationResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader


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


fun encodeWindowsAuthenticatorToJsonString(windowsRegistrationResponse: WindowsRegistrationResponse): String {
    return Json.encodeToString(windowsRegistrationResponse)
}

fun decodeWindowsAuthenticatorFromJson(jsonString: String): WindowsRegistrationResponse? {
    return try{ Json.decodeFromString(jsonString) } catch (e: Exception) { null }
}
