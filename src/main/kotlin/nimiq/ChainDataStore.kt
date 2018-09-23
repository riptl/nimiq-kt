package com.terorie.nimiq

// TODO Implement
@ExperimentalUnsignedTypes
class ChainDataStore {

    fun getChainData(key: HashLight, includeBody: Boolean = false): ChainData? {
        return null
    }

    fun getRawBlock(key: HashLight, includeForks: Boolean = false): ByteArray? {
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
