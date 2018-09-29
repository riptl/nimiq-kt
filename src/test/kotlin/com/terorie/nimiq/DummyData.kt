package com.terorie.nimiq

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.primitive.PublicKeyNim

object DummyData {

    val address1 by lazy { getAddress("1") }
    val address2 by lazy { getAddress("2") }
    val address3 by lazy { getAddress("3") }
    val address4 by lazy { getAddress("4") }
    val address5 by lazy { getAddress("5") }

    val pubKey1 by lazy { getPubKey("1") }
    val pubKey2 by lazy { getPubKey("2") }
    val pubKey3 by lazy { getPubKey("3") }
    val pubKey4 by lazy { getPubKey("4") }
    val pubKey5 by lazy { getPubKey("5") }

    val hash1 by lazy { getHash("1") }
    val hash2 by lazy { getHash("2") }
    val hash3 by lazy { getHash("3") }
    val hash4 by lazy { getHash("4") }
    val hash5 by lazy { getHash("5") }

    private fun loadResource(path: String) =
        javaClass.getResource(path).readBytes()

    private fun getAddress(suffix: String): Address {
        val bytes = loadResource("address_$suffix")
        return Address().apply { copyFrom(bytes) }
    }

    private fun getPubKey(suffix: String): PublicKeyNim {
        val bytes = loadResource("pubkey_$suffix")
        return PublicKeyNim().apply { copyFrom(bytes) }
    }

    private fun getHash(suffix: String): HashLight {
        val bytes = loadResource("blake2b_hash_$suffix")
        return HashLight().apply { copyFrom(bytes) }
    }

}