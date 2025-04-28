package com.mifos.passcode.auth.deviceAuth.data.repository

import auth.preferenceDataStore.PreferenceDataStore
import com.mifos.passcode.utility.Constants

class ChooseAuthOptionRepository(
    private val preferenceDataStore: PreferenceDataStore
) {

    fun setAuthOption(option: String){
        preferenceDataStore.putData(
            Constants.AUTH_METHOD_KEY,
            option
        )
    }

    fun getAuthOption(): String =  preferenceDataStore.getSavedData(Constants.AUTH_METHOD_KEY, "")

    fun clearAuthOption(){
        preferenceDataStore.clearData(Constants.AUTH_METHOD_KEY)
    }

}