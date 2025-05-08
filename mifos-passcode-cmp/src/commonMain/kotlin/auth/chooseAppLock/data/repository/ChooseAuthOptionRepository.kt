package com.mifos.passcode.auth.chooseAppLock.data.repository

import com.mifos.passcode.auth.kmpDataStore.data.repository.PreferencesDataSourceImpl

const val APP_LOCK_KEY = "auth_method"

class ChooseAuthOptionRepository(
    private val preferenceDataStore: PreferencesDataSourceImpl
) {

    suspend fun setAuthOption(option: String){
        preferenceDataStore.setData(
            APP_LOCK_KEY,
            option
        )
    }

    suspend fun getAuthOption(): String =
        preferenceDataStore.getData(APP_LOCK_KEY)

    fun clearAuthOption(){
        preferenceDataStore.clearInfo(APP_LOCK_KEY)
    }

}