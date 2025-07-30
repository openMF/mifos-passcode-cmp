package mifos.authenticator.biometrics


enum class Platform{
    ANDROID,
    IOS,
    JVM,
    JS,
    WASMJS;
}

expect fun getPlatform(): Platform