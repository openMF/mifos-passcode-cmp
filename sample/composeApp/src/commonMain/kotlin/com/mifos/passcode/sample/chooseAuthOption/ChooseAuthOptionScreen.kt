package com.mifos.passcode.sample.chooseAuthOption

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import auth.deviceAuth.RegistrationResult
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticatorStatus
import com.mifos.passcode.sample.authentication.passcode.PasscodeRepository
import com.mifos.passcode.sample.navigation.Route
import com.mifos.passcode.sample.chooseAuthOption.components.AuthOptionCard
import com.mifos.passcode.ui.theme.blueTint
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAuthOptionScreen(
    chooseAuthOptionScreenViewmodel: ChooseAuthOptionScreenViewmodel,
    whenDeviceLockSelected: (AppLockOption) -> Unit,
    whenPasscodeSelected: (AppLockOption) -> Unit,
    navController: NavController,
){
    val platformAvailableAuthenticationOption =
        chooseAuthOptionScreenViewmodel.authenticatorStatus.collectAsState()

    val registrationResult by chooseAuthOptionScreenViewmodel.registrationResult.collectAsState()

    val optionSet = remember{
        mutableStateListOf(false,false)
    }

    var showComingSoonDialogBox by rememberSaveable{
        mutableStateOf(false)
    }

    var dialogBoxType by rememberSaveable{
        mutableStateOf(DialogBoxType.None)
    }

    var dialogMessage by rememberSaveable{
        mutableStateOf("")
    }
    LaunchedEffect(registrationResult){
        when(registrationResult){
            is RegistrationResult.Error -> {
                dialogBoxType = DialogBoxType.ERROR
                dialogMessage = (registrationResult as RegistrationResult.Error).message
            }
            is RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                dialogBoxType = DialogBoxType.NOT_AVAILABLE
                dialogMessage = "Option Not available"
            }
            is RegistrationResult.PlatformAuthenticatorNotSet -> {
                dialogBoxType = DialogBoxType.NOT_SET
                dialogMessage = "Platform authenticator not set."
            }
            is RegistrationResult.Success -> {
                chooseAuthOptionScreenViewmodel.saveAppLockOption(AppLockOption.DeviceLock)
                navController.popBackStack()
                navController.navigate(Route.HomeScreen){
                    popUpTo(0)
                }
            }
            null -> {}
        }
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
                            platformAvailableAuthenticationOption.value.contains(
                                PlatformAuthenticatorStatus.BIOMETRICS_NOT_AVAILABLE
                            )
                        ){
                            optionSet[1] = false
                            optionSet[0] = true
                        } else {
                            showComingSoonDialogBox = true
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

                if(showComingSoonDialogBox){
                    RegistrationDialogBox(
                        onDismissRequest = {
                            showComingSoonDialogBox = false
                        },
                        dialogMessage = "Coming Soon",
                        dismissButtonText = "OK"
                    )
                }

                when(dialogBoxType){
                    DialogBoxType.ERROR -> {
                        RegistrationDialogBox(
                            onDismissRequest = {dialogBoxType = DialogBoxType.None },
                            dialogMessage = dialogMessage
                        )
                    }
                    DialogBoxType.NOT_SET -> {
                        RegistrationDialogBox(
                            onDismissRequest = {
                                chooseAuthOptionScreenViewmodel.viewModelScope.launch {
                                    chooseAuthOptionScreenViewmodel.setupPlatformAuthenticator()
                                    dialogBoxType = DialogBoxType.None
                                }
                            },
                            dialogMessage = dialogMessage
                        )
                    }
                    DialogBoxType.NOT_AVAILABLE ->{
                        RegistrationDialogBox(
                            onDismissRequest = {dialogBoxType = DialogBoxType.None },
                            dialogMessage = dialogMessage
                        )
                    }
                    DialogBoxType.None -> {}
                }

            }




            Button(
                onClick = {
                    val currentAppLock = if(optionSet[0]) {
                        AppLockOption.DeviceLock
                    } else {
                        AppLockOption.MifosPasscode
                    }

                    navigationHelper(
                        currentAppLock,
                        whenDeviceLockSelected = {
                            whenDeviceLockSelected(currentAppLock)
                            chooseAuthOptionScreenViewmodel.registerUser()
                        },
                        whenPasscodeSelected = {
                            whenPasscodeSelected(currentAppLock)
                            navController.popBackStack()
                            navController.navigate(Route.PasscodeScreen){
                                popUpTo(0)
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueTint,
                    disabledContainerColor = Color.LightGray,
                    contentColor = White
                ),
                enabled = (optionSet[0] || optionSet[1] )
            ){
                Text("Continue")
            }
        }
    }
}

enum class DialogBoxType{
    ERROR,
    NOT_SET,
    NOT_AVAILABLE,
    None
}

@Composable
fun RegistrationDialogBox(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    dialogMessage: String = "Coming Soon",
    dismissButtonText:String = "OK"
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(White) // Assuming 'White' is Color.White
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End // Aligns the button to the end
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = dialogMessage,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Assuming DialogButton is another composable you have
                DialogButton(
                    onClick = onDismissRequest, // Both dismiss actions now use the same callback
                    modifier = Modifier.padding(end = 8.dp),
                    text = dismissButtonText
                )
            }
        }
    }
}

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = blueTint,
            contentColor = White,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = White
        )
    ) {
        Text(text = text)
    }
}

private fun navigationHelper(
    option: AppLockOption,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit,
){
    if(option == AppLockOption.DeviceLock) whenDeviceLockSelected()
    if(option == AppLockOption.MifosPasscode) whenPasscodeSelected()
}

