package com.mifos.passcode.sample

//class JVMPlatform: Platform {
//    override val name: String = "Java ${System.getProperty("java.version")}"
//}

actual fun getPlatform(): Platform = Platform.JVM