package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.network.Peer

@ExperimentalUnsignedTypes
class NanoConsensusAgent(
        val blockchain: NanoChain,
        val mempool: NanoMempool,
        time: UInt,
        peer: Peer,
        invRequestManager: InvRequestManager,
        targetSubscription: Subscription
) : BaseConsensusAgent(time, peer, invRequestManager, targetSubscription) {

    companion object {
        const val CHAINPROOF_REQUEST_TIMEOUT = 1000 * 45
        const val CHAINPROOF_CHUNK_TIMEOUT = 1000 * 10
        const val ACCOUNTSPROOF_REQUEST_TIMEOUT = 1000 * 5
        const val MEMPOOL_DELAY_MIN = 1000 * 2
        const val MEMPOOL_DELAY_MAX = 1000 * 20
    }

    private var syncing = false
    private val orphanedBlocks = ArrayList<BlockHeader>()

    override fun syncBlockchain() {
        syncing = true

        val headBlock = blockchain.getBlock(peer.headHash)
        if (headBlock == null) {
            requestChainProof()
            // TODO fire sync-chain-proof
        } else {
            syncFinished()
        }
    }

    fun requestMempool() {
        // Request the peer's mempool.
        // XXX Use a random delay here to prevent requests to multiple peers at once.
        val delay = MEMPOOL_DELAY_MIN
                + Math.random() * (MEMPOOL_DELAY_MAX - MEMPOOL_DELAY_MIN)
        // TODO setTimeout(() => this._peer.channel.mempool(), delay)
    }

    private fun syncFinished() {
        syncing = false
        synced = true
        requestMempool()
        // TODO fire sync
    }

}
