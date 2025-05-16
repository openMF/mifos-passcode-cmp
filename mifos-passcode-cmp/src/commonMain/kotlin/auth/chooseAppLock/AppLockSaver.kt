package com.mifos.passcode.auth.chooseAppLock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Composable
fun rememberAppLockSaver(
    currentAppLock: AppLockSaver.AppLockOption,
    clearAppLock: () -> Unit,
    setAppLock: (AppLockSaver.AppLockOption) -> Unit
): AppLockSaver{

    val scope = rememberCoroutineScope()

    return remember(
        key1= currentAppLock,
    ) {
        AppLockSaver(
            scope =scope,
            currentAppLock = currentAppLock,
            clearAppLock = clearAppLock,
            setAppLock = setAppLock
        )
    }
}

class AppLockSaver(
    private val scope: CoroutineScope,
    private val currentAppLock: AppLockOption,
    private val clearAppLock: () -> Unit,
    private val setAppLock: (AppLockOption) -> Unit,
){

    private val _currentAuthOption = MutableStateFlow<AppLockOption>(currentAppLock)
    val currentAuthOption = _currentAuthOption.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = currentAppLock
    )

    init {
        getCurrentAppLock()
    }

    private fun getCurrentAppLock() {
        scope.launch {
            _currentAuthOption.emit(currentAppLock)
        }
    }

    fun setNewAppLock(option: AppLockOption){
        scope.launch {
            _currentAuthOption.emit(option)
            setAppLock(option)
        }
    }

    fun clearCurrentAppLock(){
        scope.launch {
            _currentAuthOption.emit(AppLockOption.None)
            clearAppLock()
        }
    }

    enum class AppLockOption{
        MifosPasscode,
        DeviceLock,
        None;
    }

}