package com.terorie.nimiq.consensus.primitive

import com.terorie.nimiq.util.Blob
import com.terorie.nimiq.util.Native
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.digests.SHA512Digest

@ExperimentalUnsignedTypes
class Hash(val algorithm: Algorithm) : Blob(algorithm.size) {

    enum class Algorithm(val size: Int) {
        INVALID(-1),
        BLAKE2B(32),
        ARGON2D(32),
        SHA256(32),
        SHA512(64);

        companion object {
            fun byID(id: UByte) = values()[id.toInt()]
        }
    }

    companion object {
        fun from(h: HashLight) =
            Hash(Algorithm.BLAKE2B).apply{ copyFrom(h.buf) }
        fun from(h: HashHard) =
            Hash(Algorithm.ARGON2D).apply{ copyFrom(h.buf) }
    }

    fun compute(input: ByteArray): Unit = when(algorithm) {
        Algorithm.BLAKE2B -> {
            val d = Blake2bDigest()
            d.update(input, 0, input.size)
            d.doFinal(buf, 0)
            Unit
        }
        Algorithm.ARGON2D -> {
            Native.nimiqArgon2d(buf, input)
        }
        Algorithm.SHA256 -> {
            val d = SHA256Digest()
            d.update(input, 0, input.size)
            d.doFinal(buf, 0)
            Unit
        }
        Algorithm.SHA512 -> {
            val d = SHA512Digest()
            d.update(input, 0, input.size)
            d.doFinal(buf, 0)
            Unit
        }
        else -> throw IllegalStateException("no algorithm available")
    }

    override fun equals(other: Any?) =
        if (other is Hash)
            algorithm == other.algorithm &&
            super.equals(other)
        else false

    override fun hashCode(): Int =
        (buf[0].toInt() shl 24) or
        (buf[1].toInt() shl 16) or
        (buf[2].toInt() shl  8) or
        (buf[3].toInt())

}