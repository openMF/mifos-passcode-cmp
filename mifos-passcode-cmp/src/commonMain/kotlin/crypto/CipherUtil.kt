//package com.mifos.passcode.crypto

/**
 * This was a expected implementation which was used previously.
 * The developer who worked on this has implemented this. But it
 * was redundant. It was supposed to encrypt and decrypt the passcode,
 * but it was used for something else and was doing nothing.
 *
 * I am planing to use this later for encrypting passcode.
 * So, I am leaving this as it is.
 */


//interface ICipherUtil {
//    @Throws(Exception::class)
//    fun generateKeyPair(): CommonKeyPair
//
//    fun getPublicKey(): CommonPublicKey?
//
//    @Throws(Exception::class)
//    fun getCrypto(): Crypto
//
//    @Throws(Exception::class)
//    suspend fun removePublicKey()
//}
//
//expect class CommonKeyPair
//expect interface CommonPublicKey
//expect class Crypto