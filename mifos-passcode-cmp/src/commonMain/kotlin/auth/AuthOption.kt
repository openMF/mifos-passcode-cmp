package com.mifos.passcode.auth

interface AuthOption{
    fun getAuthOption(): List<PlatformAuthOptions>
}