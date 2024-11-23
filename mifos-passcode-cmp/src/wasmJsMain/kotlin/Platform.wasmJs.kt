package com.mifos.passcode

class WASMJSPlatform: Platform {
    //    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val name: String = "Wasm"
}

actual fun getPlatform(): Platform {
    return WASMJSPlatform()
}