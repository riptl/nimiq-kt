package com.terorie.nimiq

import org.bouncycastle.util.encoders.Hex

class PublicKeyNim : Blob(32) {

    companion object {
        fun fromHex(hex: String) =
            PublicKeyNim().apply { copyFrom(Hex.decode(hex)) }
    }

    fun toAddress() = Address.fromHash(hash)

}
