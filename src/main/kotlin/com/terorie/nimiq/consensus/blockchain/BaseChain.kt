package com.terorie.nimiq.consensus.blockchain

import com.terorie.nimiq.consensus.GenesisConfig
import com.terorie.nimiq.consensus.Policy
import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.block.BlockUtils
import com.terorie.nimiq.consensus.primitive.HashLight
import java.math.BigInteger
import kotlin.math.min

@ExperimentalUnsignedTypes
abstract class BaseChain(val store: ChainDataStore) : IBlockchain {

    abstract val mainChain: ChainData

    fun getBlock(hash: HashLight, includeForks: Boolean = false, includeBody: Boolean = false): Block? {
        val chainData = store.getChainData(hash, includeBody)
        TODO()
    }

    fun getRawBlock(hash: HashLight, includeForks: Boolean = false) =
        store.getRawBlock(hash, includeForks)

    fun getBlockAt(height: UInt, includeBody: Boolean = false) =
        store.getBlockAt(height, includeBody)

    fun getNearestBlockAt(height: UInt, lower: Boolean = true) =
        store.getNearestBlockAt(height, lower)

    fun getSuccessorBlocks(block: Block): List<Block> =
        store.getSuccessorBlocks(block)

    fun getBlockLocators(): ArrayList<HashLight> {
        // Push top 10 hashes first, then back off exponentially.
        val locators = arrayListOf(this.headHash)

        var block: Block? = head
        for (i in (min(10, height.toLong()) - 1) until 0) {
            if (block == null)
                break
            val prevHash = block.header.prevHash
            locators.add(prevHash)
            block = getBlock(prevHash)
        }

        var step = 2U
        var i = height - 10U - step
        while (i > 0U) {
            block = getBlockAt(i)
            if (block != null)
                locators.add(block.header.hash)
            step *= 2U
            // TODO Respect max size for GetBlocksMessages
            i -= step
        }

        // Push the genesis block hash.
        if (locators.isEmpty() || locators.last() != GenesisConfig.genesisHash) {
            // TODO Respect max size for GetBlocksMessages, make space for genesis hash if necessary
            locators.add(GenesisConfig.genesisHash)
        }

        return locators
    }

    fun getNextTarget(block: Block?, next: Block?): BigInteger? {
        var _block = block

        var headData: ChainData
        if (block != null) {
            headData = store.getChainData(block.header.hash)!!
        } else {
            _block = head
            headData = mainChain
        }

        if (next != null) {
            headData = headData.nextChainData(next)
            _block = next
        }

        _block!!

        // Retrieve the timestamp of the block that appears DIFFICULTY_BLOCK_WINDOW blocks before the given block in the chain.
        // The block might not be on the main chain.
        val tailHeight: UInt =
            if (Policy.DIFFICULTY_BLOCK_WINDOW >= _block.header.height) 1U
            else _block.header.height - Policy.DIFFICULTY_BLOCK_WINDOW

        val tailData: ChainData?
        if (headData.onMainChain) {
            tailData = store.getChainDataAt(tailHeight)
        } else {
            var prevData = headData
            for (i in 0 until Policy.DIFFICULTY_BLOCK_WINDOW.toInt()) {
                if (!prevData.onMainChain)
                    break
                prevData = store.getChainData(prevData.head.header.prevHash)
                    ?: return null
            }

            tailData = if (prevData.onMainChain && prevData.head.header.height > tailHeight)
                store.getChainDataAt(tailHeight)
            else
                prevData
        }

        if (tailData == null || tailData.totalDifficulty < BigInteger.ONE)
            // Not enough blocks are available to compute the next target, fail.
            return null

        val deltaTotalDifficulty = headData.totalDifficulty - tailData.totalDifficulty
        return BlockUtils.getNextTarget(headData.head.header, tailData.head.header, deltaTotalDifficulty)
    }

}