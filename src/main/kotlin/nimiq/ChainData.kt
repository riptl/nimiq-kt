package com.terorie.nimiq

import com.terorie.nimiq.BlockUtils.realDifficulty
import java.math.BigInteger

@ExperimentalUnsignedTypes
class ChainData(
    val head: Block,
    val totalDifficulty: BigInteger,
    val totalWork: BigInteger,
    val superBlockCounts: SuperBlockCounts,
    val onMainChain: Boolean?,
    val mainChainSuccessor: HashLight
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
                list[i] = (list.getOrNull(i) ?: 0) + 1
        }

        fun subtract(depth: UByte) {
            for (i in 0 until depth.toInt()) {
                list[i]--
                assert(list[i] >= 0)
            }
        }

        fun copy() = SuperBlockCounts(ArrayList(list))

    }

}


