package com.mifos.passcode.sample.chooseAuthOption.utils

import com.mifos.passcode.auth.chooseAppLock.AuthOptionSaver
import com.mifos.passcode.sample.chooseAuthOption.utils.Constants

object Helpers {

    fun authOptionToStringMapperFunction(option: AuthOptionSaver.AppLockOption): String {
        return when(option){
            AuthOptionSaver.AppLockOption.MifosPasscode -> Constants.MIFOS_PASSCODE_VALUE
            AuthOptionSaver.AppLockOption.DeviceLock -> Constants.DEVICE_AUTHENTICATION_METHOD_VALUE
            AuthOptionSaver.AppLockOption.None -> ""
        }
    }

    fun stringToAuthOptionMapperFunction(option: String): AuthOptionSaver.AppLockOption {
        return when(option){
            Constants.MIFOS_PASSCODE_VALUE ->AuthOptionSaver.AppLockOption.MifosPasscode
            Constants.DEVICE_AUTHENTICATION_METHOD_VALUE -> AuthOptionSaver.AppLockOption.DeviceLock
            else -> AuthOptionSaver.AppLockOption.None
        }
    }

}