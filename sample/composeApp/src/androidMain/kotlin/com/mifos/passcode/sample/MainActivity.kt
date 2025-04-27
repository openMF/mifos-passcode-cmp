package com.mifos.passcode.sample

import AndroidAuthenticator
import App
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.mifos.passcode.deviceAuth.domain.PlatformAuthenticator
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module


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
            App()
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}