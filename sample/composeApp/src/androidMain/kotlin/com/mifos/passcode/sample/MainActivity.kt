package com.mifos.passcode.sample

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.mifos.passcode.LocalCompositionProvider

/*
 * I am using FragmentActivity here because when MainActivity (when AppCompatActivity or ComponentActivity) is attempted to cast into FragmentActivity
 * the app is crashing.
 * Exception: java.lang.ClassCastException: com.mifos.passcode.sample.MainActivity cannot be cast to androidx.fragment.app.FragmentActivity
 **/

class MainActivity : FragmentActivity() {
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