package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions


expect class PlatformAvailableAuthenticationOption(){

    constructor(context: Any?)

    fun getAuthOption(): List<PlatformAuthOptions>
}

