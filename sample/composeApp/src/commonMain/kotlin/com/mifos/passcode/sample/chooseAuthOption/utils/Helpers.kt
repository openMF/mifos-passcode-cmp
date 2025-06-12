package com.mifos.passcode.sample.chooseAuthOption.utils

import com.mifos.passcode.sample.chooseAuthOption.AppLockOption

object Helpers {

    fun authOptionToStringMapperFunction(option: AppLockOption): String {
        return when(option){
            AppLockOption.MifosPasscode -> Constants.MIFOS_PASSCODE_VALUE
            AppLockOption.DeviceLock -> Constants.DEVICE_AUTHENTICATION_METHOD_VALUE
            AppLockOption.None -> ""
        }
    }

    fun stringToAuthOptionMapperFunction(option: String): AppLockOption {
        return when(option){
            Constants.MIFOS_PASSCODE_VALUE -> AppLockOption.MifosPasscode
            Constants.DEVICE_AUTHENTICATION_METHOD_VALUE -> AppLockOption.DeviceLock
            else -> AppLockOption.None
        }
    }

}