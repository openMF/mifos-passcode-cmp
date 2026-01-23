/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics.windows.utils

import kotlinx.serialization.json.Json
import org.mifos.authenticator.biometrics.windows.WindowsRegistrationResponse
import java.io.BufferedReader
import java.io.InputStreamReader

fun isWindowsTenOrEleven(): Boolean {
    val rt = Runtime.getRuntime()
    val process = rt.exec("SYSTEMINFO")

    val readOutput = BufferedReader(InputStreamReader(process.inputStream))
    var line: String?

    while (true) {
        line = readOutput.readLine()
        if (line == null) break
        if (
            (
            line.contains("Windows 10") ||
                line.contains("Windows 11")
            )
        ) {
            return true
        }
    }
    return false
}

fun encodeWindowsAuthenticatorToJsonString(windowsRegistrationResponse: WindowsRegistrationResponse): String {
    return Json.encodeToString(windowsRegistrationResponse)
}

fun decodeWindowsAuthenticatorFromJson(jsonString: String): WindowsRegistrationResponse? {
    return try {
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        null
    }
}
