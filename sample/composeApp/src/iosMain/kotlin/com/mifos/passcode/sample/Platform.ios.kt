package com.mifos.passcode.sample

import platform.UIKit.UIDevice

//class IOSPlatform: Platform {
//    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
//}

actual fun getPlatform(): Platform = Platform.IOS