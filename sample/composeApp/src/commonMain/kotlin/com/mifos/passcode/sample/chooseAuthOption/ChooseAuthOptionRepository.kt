package com.mifos.passcode.sample.chooseAuthOption

import com.mifos.passcode.auth.chooseAppLock.AppLockSaver
import com.mifos.passcode.sample.chooseAuthOption.utils.Helpers
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore


const val APP_LOCK_KEY = "auth_method"

class ChooseAuthOptionRepository(
    private val preferenceDataStore: PreferenceDataStore
) {

    fun setAuthOption(option: AppLockSaver.AppLockOption){
        preferenceDataStore.putData(
            APP_LOCK_KEY,
            Helpers.authOptionToStringMapperFunction(option)
        )
    }

    fun getAuthOption(): AppLockSaver.AppLockOption {
        return Helpers.stringToAuthOptionMapperFunction(preferenceDataStore.getSavedData(APP_LOCK_KEY, ""))
    }

    fun clearAuthOption(){
        preferenceDataStore.clearData(APP_LOCK_KEY)
    }

}