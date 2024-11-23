package com.mifos.passcode

class DesktopPlatform: Platform {
    //    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val name: String = "Desktop"
}

actual fun getPlatform(): Platform {
    return DesktopPlatform()
}