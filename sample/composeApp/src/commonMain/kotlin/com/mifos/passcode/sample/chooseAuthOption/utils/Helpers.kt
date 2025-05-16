package com.mifos.passcode.sample.chooseAuthOption.utils

import com.mifos.passcode.auth.chooseAppLock.AppLockSaver

object Helpers {

    fun authOptionToStringMapperFunction(option: AppLockSaver.AppLockOption): String {
        return when(option){
            AppLockSaver.AppLockOption.MifosPasscode -> Constants.MIFOS_PASSCODE_VALUE
            AppLockSaver.AppLockOption.DeviceLock -> Constants.DEVICE_AUTHENTICATION_METHOD_VALUE
            AppLockSaver.AppLockOption.None -> ""
        }
    }

    fun stringToAuthOptionMapperFunction(option: String): AppLockSaver.AppLockOption {
        return when(option){
            Constants.MIFOS_PASSCODE_VALUE ->AppLockSaver.AppLockOption.MifosPasscode
            Constants.DEVICE_AUTHENTICATION_METHOD_VALUE -> AppLockSaver.AppLockOption.DeviceLock
            else -> AppLockSaver.AppLockOption.None
        }
    }

}