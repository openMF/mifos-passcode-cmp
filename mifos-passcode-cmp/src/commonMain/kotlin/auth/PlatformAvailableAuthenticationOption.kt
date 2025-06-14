package com.mifos.passcode.auth

import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import kotlinx.coroutines.flow.StateFlow


expect class PlatformAvailableAuthenticationOption private constructor(){

    constructor(context: Any? = null)
    val currentAuthOption: StateFlow<List<PlatformAuthOptions>>

    fun updateCurrentAuthOption()
}

