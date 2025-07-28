package com.mifos.passcode


enum class Platform{
    ANDROID,
    IOS,
    JVM,
    JS,
    WASMJS;
}

expect fun getPlatform(): Platform