package com.mifos.passcode.auth.demoImpl

import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.PlatformAuthOptions


class DemoAuthOption: AuthOption {
    override fun getAuthOption(): List<PlatformAuthOptions> {
        val availablePlatformAuthOptions = emptyList<PlatformAuthOptions>()

        return availablePlatformAuthOptions
    }
}