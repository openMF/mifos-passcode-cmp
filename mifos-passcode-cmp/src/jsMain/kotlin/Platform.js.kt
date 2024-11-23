package com.mifos.passcode

class JSPlatform: Platform {
    //    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val name: String = "JS"
}

actual fun getPlatform(): Platform {
    return JSPlatform()
}