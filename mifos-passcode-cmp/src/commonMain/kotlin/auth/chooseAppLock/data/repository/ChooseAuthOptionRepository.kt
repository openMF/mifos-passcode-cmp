package com.mifos.passcode.auth.chooseAppLock.data.repository

import com.mifos.passcode.auth.kmpDataStore.PreferenceDataStore

const val APP_LOCK_KEY = "auth_method"

class ChooseAuthOptionRepository(
    private val preferenceDataStore: PreferenceDataStore
) {

    suspend fun setAuthOption(option: String){
        preferenceDataStore.putData(
            APP_LOCK_KEY,
            option
        )
    }

    suspend fun getAuthOption(): String =
        preferenceDataStore.getSavedData(APP_LOCK_KEY, "")

    fun clearAuthOption(){
        preferenceDataStore.clearData(APP_LOCK_KEY)
    }

}