package com.mifos.passcode

class IOSPlatform: Platform {
//    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val name: String = "Ios"
}

actual fun getPlatform(): Platform = IOSPlatform()