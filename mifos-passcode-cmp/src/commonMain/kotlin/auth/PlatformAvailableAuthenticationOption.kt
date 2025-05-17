package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions


expect class PlatformAvailableAuthenticationOption private constructor(){

    constructor(context: Any?)

    fun getAuthOption(): List<PlatformAuthOptions>
}

