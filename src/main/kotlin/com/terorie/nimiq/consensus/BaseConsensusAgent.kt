package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.network.Peer
import com.terorie.nimiq.network.message.InvVector

@ExperimentalUnsignedTypes
abstract class BaseConsensusAgent(
        val time: UInt,
        val peer: Peer,
        val invRequestManager: InvRequestManager,
        val targetSubscription: Subscription? = null
) {

    companion object {
        const val REQUEST_THRESHOLD = 50
        const val REQUEST_THROTTLE = 500
        const val REQUEST_TIMEOUT = 1000 * 10
        const val REQUEST_TRANSACTIONS_WAITING_MAX = 5000
        const val REQUEST_BLOCKS_WAITING_MAX = 5000
        const val TRANSACTIONS_PROOF_REQUEST_TIMEOUT = 1000 * 10
        const val TRANSACTIONS_RECEIPTS_REQUEST_TIMEOUT = 1000 * 15
        const val TRANSACTION_RELAY_INTERVAL = 5000
        const val TRANSACTIONS_AT_ONCE = 100
        const val TRANSACTIONS_PER_SECOND = 10
        const val FREE_TRANSACTION_RELAY_INTERVAL = 6000
        const val FREE_TRANSACTIONS_AT_ONCE = 10
        const val FREE_TRANSACTIONS_PER_SECOND = 1
        const val FREE_TRANSACTION_SIZE_PER_INTERVAL = 15000
        const val TRANSACTION_RELAY_FEE_MIN = 1UL
        const val SUBSCRIPTION_CHANGE_GRACE_PERIOD = 1000 * 2
        const val HEAD_REQUEST_INTERVAL = 100 * 1000
        const val KNOWN_OBJECTS_COUNT_MAX = 40000
    }

    protected var synced = false
    private var remoteSubscription: Subscription = SubscriptionNone
    private var localSubscription: Subscription = SubscriptionNone

    abstract fun syncBlockchain()

    fun relayBlock(block: Block): Boolean {
        // Don't relay block if have not synced with the peer yet.
        if (!this.synced)
            return false

        // Only relay block if it matches the peer's subscription
        if (!this.remoteSubscription.matchesBlock(block))
            return false

        // Create InvVector
        val invVector = InvVector.fromBlock(block)

        // Don't relay block to this peer if it already knows it.
        if (knownObjects.contains(vector))
            return false

        // TODO Relay block to peer

        // Assume that the peer knows this block now.
        knownObjects.add(vector)

        return true
    }

    fun relayTransaction(tx: Transaction): Boolean {
        // Only relay transaction if it matches the peer's subscription.
        if (!remoteSubscription.matchesTransaction(tx))
            return false

        // Create InvVector.
        val vector = InvVector.fromTransaction(tx)

        // Don't relay transaction to this peer if it already knows it.
        if (knownObjects.contains(vector))
            return false

        // Relay transaction to peer later.
        if (tx.fee / tx.serializedSize.toULong() < TRANSACTION_RELAY_FEE_MIN)
            waitingFreeInvVectors.enqueue(FreeTransactionVector(vector, serializedSize))
        else
            waitingInvVectors.enqueue(vector)

        // Assume that the peer knows this transaction now.
        knownObjects.add(vector)

        return true
    }

    fun removeTransaction(tx: Transaction) {
        // Create InvVector.
        val vector = InvVector.fromTransaction(tx)

        // Remove transaction from relay queues.
        waitingFreeInvVectors.remove(vector)
        waitingInvVectors.remove(vector)
    }

    fun knowsBlock(blockHash: HashLight): Boolean {
        val vector = InvVector(InvVector.Type.BLOCK, blockHash)
        return knownObjects.contains(vector)
    }

}
