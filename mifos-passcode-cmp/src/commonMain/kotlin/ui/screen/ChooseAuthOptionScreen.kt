package com.mifos.passcode.ui.screen

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
import com.mifos.passcode.auth.AppLockOptions
import com.mifos.passcode.auth.AuthOption
import com.mifos.passcode.auth.PlatformAuthOptions
import com.mifos.passcode.ui.components.DialogButton
import com.mifos.passcode.ui.viewmodels.ChooseAuthOptionViewModel
import com.mifos.passcode.utility.Constants
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.no
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAuthOptionScreen(
    authOption: AuthOption?,
    chooseAuthOptionViewModel: ChooseAuthOptionViewModel,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit
){
    val selectedOption = chooseAuthOptionViewModel.currentAppLock.collectAsState()

    val optionsSet = remember{
        mutableStateOf(true)
    }

    if(
        ((mapperFunction(selectedOption.value) != AppLockOptions.None)) && optionsSet.value
    ){
        navigationHelper(
            mapperFunction(selectedOption.value),
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
                        selected = mapperFunction(selectedOption.value) == AppLockOptions.DeviceLock,
                        title = "Use your device lock",
                        subtitle = "Use your existing PIN, password, pattern, face ID, or fingerprint",
                        icon = Icons.Default.Dialpad,
                        onSelect = {
                            if(
                                (authOption?.getAuthOption() ?: emptyList()).contains(
                                    PlatformAuthOptions.UserCredential
                                )
                            ){
                                optionsSet.value =false
                                chooseAuthOptionViewModel.setAppLock(Constants.DEVICE_AUTHENTICATION_METHOD_VALUE)
                            }else {
                                showDialogBox.value = true
                            }
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    AuthOptionCard(
                        selected = mapperFunction(selectedOption.value) == AppLockOptions.MifosPasscode,
                        title = "Use 6-digit Mifos Passcode",
                        subtitle = "Use your Mifos Passcode",
                        icon = Icons.Default.People,
                        onSelect = {
                            optionsSet.value = false
                            chooseAuthOptionViewModel.setAppLock(Constants.MIFOS_PASSCODE_VALUE)
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
                        navigationHelper(
                            mapperFunction(selectedOption.value),
                            whenDeviceLockSelected = whenDeviceLockSelected,
                            whenPasscodeSelected = whenPasscodeSelected
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        disabledContainerColor = Color.LightGray,
                        contentColor = Color.White
                    ),
                    enabled = mapperFunction(selectedOption.value) != AppLockOptions.None
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
            color = if (selected) Color.Blue else Color.Black
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
                    tint = if (selected) Color.Blue else Color.Black
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
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

private fun mapperFunction(option: String): AppLockOptions {
    return when(option){
        Constants.MIFOS_PASSCODE_VALUE -> AppLockOptions.MifosPasscode
        Constants.DEVICE_AUTHENTICATION_METHOD_VALUE-> AppLockOptions.DeviceLock
        else -> AppLockOptions.None
    }
}

private fun navigationHelper(
    option: AppLockOptions,
    whenDeviceLockSelected: () -> Unit,
    whenPasscodeSelected: () -> Unit,
){
    if(option == AppLockOptions.DeviceLock) whenDeviceLockSelected()
    if(option == AppLockOptions.MifosPasscode) whenPasscodeSelected()
}

