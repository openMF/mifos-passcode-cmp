package com.mifos.passcode.demoImpl

import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.AuthOptions


class DemoAuthOption: AuthOption {
    override fun getAuthOption(): List<AuthOptions> {
        val availableAuthOptions = mutableListOf(AuthOptions.UserCredential, AuthOptions.MifosPasscode)

        return availableAuthOptions
    }
}