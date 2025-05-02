package com.mifos.passcode.sample

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mifos.passcode.LocalCompositionProvider


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val androidAuthenticator = AndroidAuthenticator(this)
//
//        // Dynamically create a module and register the initialized instance
//        val activityModule = module {
//            single { androidAuthenticator }.bind<PlatformAuthenticator>()
//        }
//
//        // Load the module into Koin
//        loadKoinModules(activityModule)


        setContent {
            LocalCompositionProvider(this) {
                App()
            }
        }
    }
}


//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}