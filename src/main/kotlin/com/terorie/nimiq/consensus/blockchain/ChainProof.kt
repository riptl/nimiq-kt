package com.terorie.nimiq.consensus.blockchain

import com.terorie.nimiq.consensus.Policy
import com.terorie.nimiq.consensus.block.BlockUtils
import com.terorie.nimiq.util.io.Enc
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger

@ExperimentalUnsignedTypes
class ChainProof(val prefix: BlockChain, val suffix: HeaderChain) {

    companion object : Enc<ChainProof> {
        override fun serializedSize(o: ChainProof): Int = TODO()
        override fun deserialize(s: InputStream): ChainProof = TODO()
        override fun serialize(s: OutputStream, o: ChainProof) = TODO()
    }

    fun verify(): Boolean {
        // Check that the prefix chain is anchored.
        if (!prefix.isAnchored)
            return false

        // Check that both prefix and suffix are valid chains.
        if (!prefix.verify() || !suffix.verify())
            return false

        // Check that the suffix connects to the prefix
        if (suffix.length > 0 && !suffix.tail.isImmediateSuccessorOf(prefix.head.header))
            return false

        // Verify the block targets where possible
        if (!verifyDifficulty())
            return false

        // Everything checks out.
        return true
    }

    private fun verifyDifficulty(): Boolean {
        // Extract the dense suffix of the prefix.
        val denseSuffix = prefix.denseSuffix().map { it.header }
        val denseChain = ArrayList(denseSuffix).apply { addAll(suffix.headers) }

        // Compute totalDifficulty for each block of the dense chain.
        var totalDifficulty = BigInteger.ZERO
        val totalDifficulties = ArrayList<BigInteger>(denseChain.size)
        for (i in 0 until denseChain.size) {
            totalDifficulty += denseChain[i].difficulty
            totalDifficulties[i] = totalDifficulty
        }

        var headIndex = denseChain.size - 2
        var tailIndex = headIndex - Policy.DIFFICULTY_BLOCK_WINDOW.toInt()
        while (tailIndex >= 0 && headIndex >= 0) {
            val headBlock = denseChain[headIndex]
            val tailBlock = denseChain[tailIndex]
            val deltaTotalDifficulty = totalDifficulties[headIndex] - totalDifficulties[tailIndex]
            val target = BlockUtils.getNextTarget(headBlock, tailBlock, deltaTotalDifficulty)
            val nBits = BlockUtils.targetToCompact(target)

            val checkBlock = denseChain[headIndex + 1]
            if (checkBlock.nBits != nBits) {
                // TODO Log block target mismatch
                return false
            }

            --headIndex
            if (tailIndex != 0 || tailBlock.height != 1U)
                --tailIndex
        }

        return true
    }

}