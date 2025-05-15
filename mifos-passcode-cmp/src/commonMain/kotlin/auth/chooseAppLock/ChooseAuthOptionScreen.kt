package com.mifos.passcode.auth.chooseAppLock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.auth.passcode.components.DialogButton
import com.mifos.passcode.ui.theme.blueTint
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAuthOptionScreen(
    authOption: AuthOption,
    authOptionSaver: AuthOptionSaver,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit
){
    val selectedOption by authOptionSaver.currentAppLockOption.collectAsState()

    val optionSet = remember{
        mutableStateListOf(false,false)
    }

    if(
        selectedOption != AuthOptionSaver.AppLockOption.None
    ){
        navigationHelper(
            selectedOption,
            whenDeviceLockSelected = whenDeviceLockSelected,
            whenPasscodeSelected = whenPasscodeSelected
        )
    }else {

        val showDialogBox = rememberSaveable{
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
                                (authOption?.getAuthOption() ?: emptyList()).contains(
                                    PlatformAuthOptions.UserCredential
                                )
                            ){
                                optionSet[1] = false
                                optionSet[0] = true
                            } else {
                                showDialogBox.value = true
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

                    if(showDialogBox.value){
                        Dialog(
                            onDismissRequest = {
                                showDialogBox.value = false
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
                                            showDialogBox.value = false
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
                        val authOption = if(optionSet[0]) {
                            AuthOptionSaver.AppLockOption.DeviceLock
                        } else {
                            AuthOptionSaver.AppLockOption.MifosPasscode
                        }

                        authOptionSaver.setAppLock(authOption)

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
}

@Composable
fun AuthOptionCard(
    selected: Boolean,
    title: String,
    subtitle: String = "",
    icon: ImageVector = Icons.Outlined.Dialpad,
    onSelect: () -> Unit
){
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) blueTint else Color.Black
        ),
        modifier = Modifier
            .height(130.dp)
            .fillMaxWidth()
            .clickable{ onSelect.invoke() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = title, fontSize =18.sp)
                Icon(
                    imageVector = if(selected) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Radiobutton",
                    modifier = Modifier.size(25.dp),
                    tint = if (selected) blueTint else Color.Black
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = subtitle, fontSize =12.sp)
            }
        }
    }
}


private fun navigationHelper(
    option: AuthOptionSaver.AppLockOption,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit,
){
    if(option == AuthOptionSaver.AppLockOption.DeviceLock) whenDeviceLockSelected()
    if(option == AuthOptionSaver.AppLockOption.MifosPasscode) whenPasscodeSelected()
}

