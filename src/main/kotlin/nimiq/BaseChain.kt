package com.terorie.nimiq

import java.math.BigInteger

class BaseChain(val store: ChainDataStore) : Blockchain {

    fun getBlock(hash: HashLight, includeForks: Boolean = false, includeBoyd: Boolean = false): Block? {
        val chainData = store.getChainData(hash, includeBody)
        TODO()
    }

    fun getRawBlock(hash: HashLight, includeForks: Boolean = false) =
        store.getRawStore(hahs, includeForks)

    fun getBlockAt(height: UInt, includeBody: Boolean = false) =
        store.getBlockAt(height, includeBody)

    fun getNearestBlockAt(height: UInt, lower: Boolean = true) =
        store.getNearestBlockAt(height, lower)

    fun getSuccessorBlocks(block: Block): List<Block> =
        store.getSuccessorBlocks(block)

    fun getNextTarget(block: Block?, next: Block?): BigInteger? {
        var _block = block
        var _next = next

        var headData: ChainData?
        if (block != null) {
            headData = store.getChainData(block.header.hash)
            assert(headData != null)
        } else {
            _block = head
            headData = mainChain
        }

        if (_next != null) {
            headData = headData.nextChainData(next)
            _block = _next
        }

        _block!!

        // Retrieve the timestamp of the block that appears DIFFICULTY_BLOCK_WINDOW blocks before the given block in the chain.
        // The block might not be on the main chain.
        val tailHeight: UInt =
            if (Policy.DIFFICULTY_BLOCK_WINDOW >= _block.header.height) 1
            else _block.header.height - Policy.DIFFICULTY_BLOCK_WINDOW

        var tailData: ChainData? = null
        if (headData.onMainChain) {
            tailData = store.getChainDataAt(tailHeight)
        } else {
            var prevData = headData
            for (i in 0 until Policy.DIFFICULTY_BLOCK_WINDOW) {
                if (!prevData.onMainChain)
                    break
                prevData = store.getChainData(prevData.head.prevHash)
                    ?: return null
            }

            tailData = if (prevData.onMainChain && prevData.head.height > tailHeight)
                store.getChainDataAt(tailHeight)
            else
                prevData
        }

        if (tailData == null || tailData.totalDifficulty < 1)
            // Not enough blocks are available to compute the next target, fail.
            return null

        val deltaTotalDifficulty = headData.totalDifficulty - tailData.totalDifficulty
        return getNextTarget(headData.head.header, tailData.head.header, deltaTotalDifficulty)
    }

}