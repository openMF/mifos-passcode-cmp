package com.mifos.passcode.auth.chooseAppLock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mifos.passcode.LibraryLocalContextProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.chooseAppLock.components.AuthOptionCard
import com.mifos.passcode.auth.passcode.components.DialogButton
import com.mifos.passcode.ui.theme.blueTint
import org.jetbrains.compose.ui.tooling.preview.Preview



@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAuthOptionScreen(
    whenDeviceLockSelected: (AppLockSaver.AppLockOption) -> Unit,
    whenPasscodeSelected: (AppLockSaver.AppLockOption) -> Unit,
    loading: Boolean = false,
){
    val platformAvailableAuthenticationOption =
        PlatformAvailableAuthenticationOption(
            LibraryLocalContextProvider.current
        )

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
                            platformAvailableAuthenticationOption.getAuthOption().isNotEmpty()
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
                    Dialog(
                        onDismissRequest = {
                            showComingSoonDialogBox.value = false
                        }
                    ){
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(White)
                                .padding(16.dp)
                        ) {
                            Column {

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "Coming Soon",
                                    modifier = Modifier
                                        .padding(8.dp),
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                DialogButton(
                                    onClick = {
                                        showComingSoonDialogBox.value = false
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp),
                                    text = "OK"
                                )
                            }

                        }

                    }
                }

            }


            Button(
                onClick = {
                    val currentAppLock = if(optionSet[0]) {
                        AppLockSaver.AppLockOption.DeviceLock
                    } else {
                        AppLockSaver.AppLockOption.MifosPasscode
                    }

                    navigationHelper(
                        currentAppLock,
                        whenDeviceLockSelected = {
                            whenDeviceLockSelected(currentAppLock)
                        },
                        whenPasscodeSelected = { whenPasscodeSelected(currentAppLock) }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueTint,
                    disabledContainerColor = Color.LightGray,
                    contentColor = White
                ),
                enabled = (optionSet[0] || optionSet[1] ) && !loading
            ){
                if(loading) CircularProgressIndicator()
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

