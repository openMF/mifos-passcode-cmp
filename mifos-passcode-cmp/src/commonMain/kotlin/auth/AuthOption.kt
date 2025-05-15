package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions


expect class AuthOption(){

    constructor(context: Any?)

    fun getAuthOption(): List<PlatformAuthOptions>
}

