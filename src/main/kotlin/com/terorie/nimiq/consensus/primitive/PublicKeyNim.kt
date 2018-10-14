package com.terorie.nimiq.consensus.primitive

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.util.Blob
import org.bouncycastle.util.encoders.Hex

class PublicKeyNim : Blob(SIZE) {

    companion object {
        const val SIZE = 32

        fun fromHex(hex: String) =
            PublicKeyNim().apply { copyFrom(Hex.decode(hex)) }
    }

    fun toAddress() = Address.fromHash(hash)

}
