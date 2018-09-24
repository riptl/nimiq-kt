package com.terorie.nimiq.consensus.blockchain

import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.util.io.*
import java.io.OutputStream
import java.math.BigInteger

@ExperimentalUnsignedTypes
class HeaderChain(val headers: ArrayList<BlockHeader>) {

    fun serialize(s: OutputStream) {
        s.writeUShort(headers.size.toUShort())
        for (header in headers)
            header.serialize(s)
    }

    fun verify(): Boolean {
        // For performance reasons, we DO NOT VERIFY the validity of the blocks in the chain here.
        // Block validity is checked by the Nano/LightChain upon receipt of a ChainProof.

        // Check that all headers in the chain are valid successors of one another.
        for (i in headers.size - 1 downTo 1) {
            if (!headers[i].isImmediateSuccessorOf(headers[i - 1]))
                return false
        }

        // Everything checks out.
        return true
    }

    val length get() = headers.size

    val head get() = headers.last()
    val tail get() = headers.first()

    fun totalDifficulty(): BigInteger {
        var totalDifficulty = BigInteger.ZERO
        for (header in headers)
            totalDifficulty += header.difficulty
        return totalDifficulty
    }

}
