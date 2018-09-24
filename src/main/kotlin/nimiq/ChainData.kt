package com.terorie.nimiq

import com.terorie.nimiq.BlockUtils.getHashDepth
import com.terorie.nimiq.BlockUtils.realDifficulty
import java.math.BigInteger

@ExperimentalUnsignedTypes
class ChainData(
    val head: Block,
    val totalDifficulty: BigInteger,
    val totalWork: BigInteger,
    val superBlockCounts: SuperBlockCounts,
    val onMainChain: Boolean = false,
    val mainChainSuccessor: HashLight? = null
) {

    fun nextChainData(block: Block): ChainData {
        val totalDifficulty = totalDifficulty + block.header.difficulty
        val pow = Hash.from(block.header.pow)
        val totalWork = totalWork + realDifficulty(pow)
        val superBlockCounts = superBlockCounts.copy().apply { getHashDepth(pow) }
        return ChainData(block, totalDifficulty, totalWork, superBlockCounts)
    }

    class SuperBlockCounts(val list: ArrayList<UByte> = ArrayList()) {

        fun add(depth: UByte) {
            for (i in 0 until depth.toInt())
                list[i] = ((list.getOrNull(i)?.toUInt() ?: 0U) + 1U).toUByte()
        }

        fun subtract(depth: UByte) {
            for (i in 0 until depth.toInt()) {
                list[i]--
                assert(list[i] >= 0.toUByte())
            }
        }

        fun copy() = SuperBlockCounts(ArrayList(list))

    }

}


