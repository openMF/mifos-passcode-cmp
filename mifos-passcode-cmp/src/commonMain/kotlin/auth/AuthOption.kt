package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions

interface AuthOption{
    fun getAuthOption(): List<PlatformAuthOptions>
}