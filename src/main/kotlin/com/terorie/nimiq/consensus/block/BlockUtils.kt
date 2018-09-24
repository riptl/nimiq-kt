package com.terorie.nimiq.consensus.block

import com.terorie.nimiq.consensus.primitive.Hash
import com.terorie.nimiq.consensus.primitive.HashHard
import com.terorie.nimiq.consensus.Policy
import java.math.BigInteger
import kotlin.math.*

@ExperimentalUnsignedTypes
object BlockUtils {

    fun compactToTarget(compact: UInt): BigInteger {
        val exp = 8 * ((compact shr 24) - 3U).toInt()
        val base = BigInteger.ONE shl exp
        val factor = BigInteger.valueOf(compact.toLong() and 0xffffff)
        return base * factor
    }

    fun targetToCompact(target: BigInteger): UInt {
        var size = max(ceil(log2(target.toDouble()) / 8).toInt(), 1)
        val firstMask = 1 shl (size - 1) * 8
        val firstByteBig = target / BigInteger.valueOf(firstMask.toLong())
        val firstByte = firstByteBig.toInt()

        if (firstByte >= 0x80)
            size++

        val followMask = 1 shl (size - 3) * 8
        val followBytesBig = target / BigInteger.valueOf(followMask.toLong())
        val followBytes = followBytesBig.toInt().toUInt()

        return (size.toUInt() shl 24) or (followBytes and 0xffffffU)
    }

    fun getTargetHeight(target: BigInteger): Int =
        ceil(log2(target.toDouble())).toInt()

    fun getTargetDepth(target: BigInteger): Int =
        getTargetHeight(Policy.BLOCK_TARGET_MAX) - getTargetHeight(target)

    fun compactToDifficulty(compact: UInt): BigInteger =
        Policy.BLOCK_TARGET_MAX / compactToTarget(compact)

    fun difficultyToCompact(difficulty: BigInteger): UInt =
            targetToCompact(difficultyToTarget(difficulty))

    fun difficultyToTarget(difficulty: BigInteger): BigInteger =
        Policy.BLOCK_TARGET_MAX / difficulty

    fun targetToDifficulty(target: BigInteger): BigInteger =
        Policy.BLOCK_TARGET_MAX / target

    fun hashToTarget(hash: Hash) =
        BigInteger(1, hash.buf)

    fun realDifficulty(hash: Hash) =
            targetToDifficulty(hashToTarget(hash))

    fun getHashDepth(hash: Hash) =
            getTargetDepth(hashToTarget(hash))

    fun isProofOfWork(hash: HashHard, target: BigInteger): Boolean {
        val hashInt = BigInteger(1, hash.buf)
        return hashInt <= target
    }

    fun powerOfTwo(exp: Int) =
        BigInteger.ONE shl exp

    fun getNextTarget(headBlock: BlockHeader, tailBlock: BlockHeader, deltaTotalDifficulty: BigInteger): BigInteger {
        var deltaTotalDiff = deltaTotalDifficulty

        assert(
            headBlock.height - tailBlock.height == Policy.DIFFICULTY_BLOCK_WINDOW
                ||
            headBlock.height <= Policy.DIFFICULTY_BLOCK_WINDOW && tailBlock.height == 1U
        ) { "Tail and head block must be ${Policy.DIFFICULTY_BLOCK_WINDOW} blocks apart"}

        var actualTime = headBlock.timestamp - tailBlock.timestamp

        // Simulate that the Policy.BLOCK_TIME was achieved for the blocks before the genesis block, i.e. we simulate
        // a sliding window that starts before the genesis block. Assume difficulty = 1 for these blocks.
        if (headBlock.height <= Policy.DIFFICULTY_BLOCK_WINDOW) {
            actualTime += (Policy.DIFFICULTY_BLOCK_WINDOW - headBlock.height + 1U) * Policy.BLOCK_TIME
            deltaTotalDiff += BigInteger.valueOf(
                (Policy.DIFFICULTY_BLOCK_WINDOW - headBlock.height + 1U).toLong())
        }

        // Compute the target adjustment factor.
        val expectedTime = Policy.DIFFICULTY_BLOCK_WINDOW * Policy.BLOCK_TIME
        var adjustment = actualTime.toInt().toDouble() / expectedTime.toInt()

        // Clamp the adjustment factor to [1 / MAX_ADJUSTMENT_FACTOR, MAX_ADJUSTMENT_FACTOR].
        adjustment = max(adjustment, 1.0 / Policy.DIFFICULTY_MAX_ADJUSTMENT_FACTOR)
        adjustment = min(adjustment, Policy.DIFFICULTY_MAX_ADJUSTMENT_FACTOR)

        // Compute the next target.
        val averageDifficulty = deltaTotalDifficulty / BigInteger.valueOf(Policy.DIFFICULTY_BLOCK_WINDOW.toLong())
        val averageTarget = difficultyToTarget(averageDifficulty)
        var nextTarget = (averageTarget.toBigDecimal() * adjustment.toBigDecimal()).toBigInteger()

        // Make sure the target is below or equal the maximum allowed target (difficulty 1).
        // Also enforce a minimum target of 1.
        if (nextTarget > Policy.BLOCK_TARGET_MAX)
            nextTarget = Policy.BLOCK_TARGET_MAX

        if (nextTarget < BigInteger.ONE)
            nextTarget = BigInteger.ONE

        // XXX Reduce target precision to nBits precision.
        val nBits = targetToCompact(nextTarget)
        return compactToTarget(nBits)
    }

}
