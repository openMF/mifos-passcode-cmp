package com.mifos.passcode.auth.deviceAuth.di

import com.mifos.passcode.ui.viewmodels.DeviceAuthenticatorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val DeviceAuthModule= module {
    viewModelOf(::DeviceAuthenticatorViewModel)
}