package com.terorie.nimiq.consensus.blockchain

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.primitive.HashLight

// TODO Implement
@ExperimentalUnsignedTypes
class ChainDataStore {

    fun getChainData(key: HashLight, includeBody: Boolean = false): ChainData? {
        return null
    }

    fun getRawBlock(key: HashLight, includeForks: Boolean = false): ByteArray? {
        return null
    }

    fun getChainDataAt(height: UInt, includeBody: Boolean = false): ChainData? {
        return null
    }

    fun getBlockAt(height: UInt, includeBody: Boolean = false): Block? {
        return null
    }

    fun getNearestBlockAt(height: UInt, lower: Boolean = false): Block? {
        return null
    }

    fun getSuccessorBlocks(block: Block): List<Block> {
        return emptyList()
    }

}
