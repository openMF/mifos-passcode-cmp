package com.mifos.passcode.auth.chooseAppLock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



@Composable
fun rememberAuthOptionSaver(
    currentAppLock: AuthOptionSaver.AppLockOption,
    clearAuthOption: () -> Unit,
    setAuthOption: (AuthOptionSaver.AppLockOption) -> Unit
): AuthOptionSaver{

    val scope = rememberCoroutineScope()

    return remember(
        key1= currentAppLock,
    ) {
        AuthOptionSaver(
            scope =scope,
            currentSetAuthOption = currentAppLock,
            clearAuthOption = clearAuthOption,
            setAuthOption = setAuthOption
        )
    }
}

class AuthOptionSaver(
    private val scope: CoroutineScope,
    private val currentSetAuthOption: AppLockOption,
    private val clearAuthOption: () -> Unit,
    private val setAuthOption: (AppLockOption) -> Unit,
){

    private val _currentAuthOption = MutableStateFlow<AppLockOption>(AuthOptionSaver.AppLockOption.None)
    val currentAppLockOption = _currentAuthOption.asStateFlow()

    init {
        getAppLock()
    }

    private fun getAppLock() {
        scope.launch {
            _currentAuthOption.value = currentSetAuthOption
        }
    }

    fun setAppLock(option: AppLockOption){
        scope.launch {
            setAuthOption(option)
            _currentAuthOption.value = option
        }
    }

    fun clearAppLock(){
        clearAuthOption()
        _currentAuthOption.value = AuthOptionSaver.AppLockOption.None
    }

    enum class AppLockOption{
        MifosPasscode,
        DeviceLock,
        None;
    }

}