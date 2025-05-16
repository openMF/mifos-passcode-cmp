package com.mifos.passcode.auth.deviceAuth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mifos.passcode.Platform
import com.mifos.passcode.auth.deviceAuth.AuthenticatorStatus
import com.mifos.passcode.auth.deviceAuth.PlatformAuthOptions
import com.mifos.passcode.ui.theme.blueTint
import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.eye_scanner
import io.github.openmf.mifos_passcode_cmp.generated.resources.face_scan
import io.github.openmf.mifos_passcode_cmp.generated.resources.fingerprint
import io.github.openmf.mifos_passcode_cmp.generated.resources.keypad
import org.jetbrains.compose.resources.painterResource

@Composable
fun SystemAuthenticatorButton(
    onClick: () -> Unit,
    platformAuthOptions: List<PlatformAuthOptions> = listOf(PlatformAuthOptions.UserCredential),
    authenticatorStatus: AuthenticatorStatus,
    platform: Platform
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.Center
    ){
        TextButton(
            onClick = onClick
        ) {
            if(authenticatorStatus.biometricsSet){
                if(platformAuthOptions.contains(PlatformAuthOptions.Iris)){
                    Image(
                        painter = painterResource(Res.drawable.eye_scanner),
                        contentDescription = "Use Biometrics",
                        modifier = Modifier.size(50.dp)
                    )
                } else if(platformAuthOptions.contains(PlatformAuthOptions.FaceId)){
                    if(platformAuthOptions.contains(PlatformAuthOptions.Fingerprint)){
                        Box(
                            modifier = Modifier.height(50.dp)
                                .width(200.dp)
                                .clip(
                                    RoundedCornerShape(30.dp)
                                )
                                .background(color = blueTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Use Biometrics",
                                    color = Color.White
                                )
                            }
                        }
                    }else {
                        Image(
                            painter = painterResource(Res.drawable.face_scan),
                            contentDescription = "Use Biometrics",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                } else if (platformAuthOptions.contains(PlatformAuthOptions.Fingerprint)){
                    Image(
                        painter = painterResource(Res.drawable.fingerprint),
                        contentDescription = "Use Biometrics",
                        modifier = Modifier.size(50.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .clip(
                                RoundedCornerShape(20.dp)
                            )
                            .background(color = blueTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Use Biometrics",
                                color = Color.White
                            )
                        }
                    }
                }
            } else if(authenticatorStatus.userCredentialSet){
                Image(
                    painter = painterResource(Res.drawable.keypad),
                    contentDescription = "Use Biometrics",
                    modifier = Modifier.size(50.dp)
                )
            } else {
                Text("Setup Device Authentication")
            }
        }
    }
}