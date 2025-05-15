package com.mifos.passcode.sample.chooseAuthOption

import androidx.lifecycle.ViewModel
import com.mifos.passcode.auth.chooseAppLock.AuthOptionSaver
import com.mifos.passcode.sample.chooseAuthOption.utils.Helpers
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


const val APP_LOCK_KEY = "auth_method"

class ChooseAuthOptionViewmodel(
    private val preferenceDataStore: PreferenceDataStore
): ViewModel() {

    private val _currentSelectedAppLock = MutableStateFlow(AuthOptionSaver.AppLockOption.None)
    val currentSelectedAppLock = _currentSelectedAppLock.asStateFlow()

    init {
        getAuthOption()
    }

    fun setAuthOption(option: AuthOptionSaver.AppLockOption){
        preferenceDataStore.putData(
            APP_LOCK_KEY,
            Helpers.authOptionToStringMapperFunction(option)
        )
    }

    private fun getAuthOption(): AuthOptionSaver.AppLockOption {
        val appLock = Helpers.stringToAuthOptionMapperFunction(preferenceDataStore.getSavedData(APP_LOCK_KEY, ""))
        _currentSelectedAppLock.value = appLock
        return appLock
    }

    fun clearAuthOption(){
        _currentSelectedAppLock.value= AuthOptionSaver.AppLockOption.None
        preferenceDataStore.clearData(APP_LOCK_KEY)
    }

}