package com.mifos.passcode.sample

enum class Platform{
    ANDROID,
    IOS,
    JVM,
    JS,
    WASMJS;
}

expect fun getPlatform(): Platform