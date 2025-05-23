package com.mifos.passcode.sample

import android.os.Build

class AndroidPlatform() : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()