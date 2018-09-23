package com.terorie.nimiq

import org.bouncycastle.crypto.digests.Blake2bDigest

class HashLight() : Blob(SIZE) {

    companion object {
        const val SIZE = 32
    }

    constructor(vararg inputs: ByteArray) : this() {
        val d = Blake2bDigest()
        for (input in inputs)
            d.update(input, 0, input.size)
        d.doFinal(buf, 0)
    }

    override fun hashCode(): Int =
        (buf[0].toInt() shl 24) or
        (buf[1].toInt() shl 16) or
        (buf[2].toInt() shl  8) or
        (buf[3].toInt())

}
