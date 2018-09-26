package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.network.Peer
import com.terorie.nimiq.network.connection.CloseType
import com.terorie.nimiq.network.message.InvVector
import com.terorie.nimiq.network.message.Message
import java.lang.IllegalArgumentException

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

    init {
        // Listen to consensus messages from the peer.
        peer.channel.on("chain-proof", )
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

    fun getAccounts(blockHash: HashLight, addresses: List<Address>): ArrayList<Account> {
        // TODO log

        // Request AccountsProof from peer
        peer.channel.getAccountsProof(blockHash, addresses)

        // Drop the peer if it doesn't send the accounts proof within the timeout.
        peer.channel.expectMessage(
            types = arrayOf(Message.Type.ACCOUNTS_PROOF),
            onTimeout = {
                peer.channel.close(CloseType.GET_ACCOUNTS_PROOF_TIMEOUT, "getAccountsProof timeout")
                throw IllegalArgumentException("timeout")
            },
            msgTimeout = NanoConsensusAgent.ACCOUNTSPROOF_REQUEST_TIMEOUT
        )

        // TODO Save request
    }

    private var requestedChainProof = false
    private fun requestChainProof() {
        // Only one chain proof request at a time.-
        if (requestedChainProof)
            return

        // Request ChainProof from peer
        peer.channel.getChainProof()
        requestedChainProof = true

        // Drop the peer if it doesn't send the chain proof within the timeout.
        peer.channel.expectMessage(
            types = arrayOf(Message.Type.CHAIN_PROOF),
            onTimeout = {
                peer.channel.close(CloseType.GET_CHAIN_PROOF_TIMEOUT, "getChainProof timeout")
            },
            msgTimeout = NanoConsensusAgent.CHAINPROOF_REQUEST_TIMEOUT,
            chunkTimeout = NanoConsensusAgent.CHAINPROOF_CHUNK_TIMEOUT
        )
    }

    private fun onChainProof(msg: ChainProofMessage) {
        // TODO log

        // Check if we have requested a chain proof, reject unsolicited ones.
        // FIXME
        if (!requestedChainProof) {
            // TODO log
            // TODO close/ban?
            return
        }
        requestedChainProof = false

        if (syncing) {}
            // TODO fire verify-chain-proof

        // Push the proof into the NanoChain.
        if (!blockchain.pushProof(msg.proof)) {
            // TODO log
            // TODO ban instead?
            peer.channel.close(CloseType.INVALID_CHAIN_PROOF_1, "invalid chain proof")
            return
        }

        // TODO add all blocks from the chain proof to knownObjects.
        // Apply any orphaned blocks we received while waiting for the chain proof.
        applyOrphanedBlocks()

        if (syncing)
            syncFinished()
    }

    private fun applyOrphanedBlocks() {
        for (header in orphanedBlocks) {
            val status = blockchain.pushHeader(header)
            if (status == NanoChain.ERR_INVALID) {
                peer.channel.close(CloseType.RECEIVED_INVALID_BLOCK, "received invalid block")
                break
            }
        }
        orphanedBlocks.clear()
    }

    private fun doRequestData(vectors: List<InvVector>) {
        val blocks = ArrayList<InvVector>()
        val txs = ArrayList<InvVector>()
        for (vector in vectors) when (vector.type) {
            InvVector.Type.BLOCK -> blocks.add(vector)
            InvVector.Type.TRANSACTION -> txs.add(vector)
            else -> Unit
        }

        // Request headers and transactions from peer
        peer.channel.getHeader(blocks)
        peer.channel.getData(txs)
    }

    private fun getBlock(hash: HashLight, includeForks: Boolean = false) =
        blockchain.getBlock(hash, includeForks)

    private fun getTransaction(hash: HashLight) =
        mempool.getTransaction(hash)

    private fun processHeader(hash: HashLight, header: BlockHeader) {
        // TODO send reject message if we don't like the block
        val status = blockchain.pushHeader(header)
        when (status) {
            NanoChain.ERR_INVALID -> {
                peer.channel.close(CloseType.RECEIVED_INVALID_HEADER, "received invalid header")
            }
            // Re-sync with this peer if it starts sending orphan blocks after the initial sync.
            NanoChain.ERR_ORPHAN -> {
                orphanedBlocks.add(header)
                if (synced)
                    requestChainProof()
            }
        }
    }

    private fun processTransaction(hash: HashLight, tx: Transaction) =
        mempool.pushTransaction(tx)

}
