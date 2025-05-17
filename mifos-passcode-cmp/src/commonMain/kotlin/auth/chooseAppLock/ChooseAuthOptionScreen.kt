package com.mifos.passcode.auth.chooseAppLock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mifos.passcode.LocalContextProvider
import com.mifos.passcode.LocalPlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.chooseAppLock.components.AuthOptionCard
import com.mifos.passcode.auth.chooseAppLock.components.MessageDialogBox
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.ui.theme.blueTint
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAuthOptionScreen(
    appLockSaver: AppLockSaver,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit
){

    val platformAvailableAuthenticationOption =
        PlatformAvailableAuthenticationOption(
            LocalContextProvider.current
        )

    val currentAppLock by appLockSaver.currentAuthOption.collectAsState()

    LaunchedEffect(currentAppLock){
        when(currentAppLock){
            AppLockSaver.AppLockOption.MifosPasscode -> whenPasscodeSelected()
            AppLockSaver.AppLockOption.DeviceLock -> whenDeviceLockSelected()
            AppLockSaver.AppLockOption.None -> {}
        }
    }

    val optionSet = remember{
        mutableStateListOf(false,false)
    }

    val showComingSoonDialogBox = rememberSaveable{
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {TopAppBar(title = { Text("Enable app lock", fontSize = 24.sp)})},
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
                .fillMaxSize()
                .padding(it)
        ) {

            Column{
                AuthOptionCard(
                    selected = optionSet[0],
                    title = "Use your device lock",
                    subtitle = "Use your existing PIN, password, pattern, face ID, or fingerprint",
                    icon = Icons.Default.Dialpad,
                    onSelect = {
                        if(
                            platformAvailableAuthenticationOption.getAuthOption().contains(
                                PlatformAuthOptions.UserCredential
                            )
                        ){
                            optionSet[1] = false
                            optionSet[0] = true
                        } else {
                            showComingSoonDialogBox.value = true
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                AuthOptionCard(
                    selected = optionSet[1],
                    title = "Use 6-digit Mifos Passcode",
                    subtitle = "Use your Mifos Passcode",
                    icon = Icons.Default.People,
                    onSelect = {
                        optionSet[0] = false
                        optionSet[1] = true
                    }
                )

                if(showComingSoonDialogBox.value){
                    MessageDialogBox(
                        onDismiss = { showComingSoonDialogBox.value = false },
                        onButtonClick = { showComingSoonDialogBox.value = false },
                        dialogMessage = "Coming Soon",
                        confirmButtonText = "OK"
                    )
                }

            }


            Button(
                onClick = {
                    val authOption = if(optionSet[0]) {
                        AppLockSaver.AppLockOption.DeviceLock
                    } else {
                        AppLockSaver.AppLockOption.MifosPasscode
                    }

                    appLockSaver.setNewAppLock(authOption)

                    navigationHelper(
                        option = authOption,
                        whenDeviceLockSelected = whenDeviceLockSelected,
                        whenPasscodeSelected = whenPasscodeSelected
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueTint,
                    disabledContainerColor = Color.LightGray,
                    contentColor = White
                ),
                enabled = optionSet[0] || optionSet[1]
            ){
                Text("Continue")
            }
        }
    }

}


private fun navigationHelper(
    option: AppLockSaver.AppLockOption,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit,
){
    if(option == AppLockSaver.AppLockOption.DeviceLock) whenDeviceLockSelected()
    if(option == AppLockSaver.AppLockOption.MifosPasscode) whenPasscodeSelected()
}

