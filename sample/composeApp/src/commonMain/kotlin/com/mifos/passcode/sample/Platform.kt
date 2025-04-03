package com.mifos.passcode.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform