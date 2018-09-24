package com.terorie.nimiq.consensus.mempool

import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Accounts
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.blockchain.IBlockchain
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.transaction.Transaction
import java.util.*
import kotlin.collections.HashSet

@ExperimentalUnsignedTypes
class Mempool(val blockchain: IBlockchain, val accounts: Accounts) {

    enum class ReturnCode(id: Int) {
        FEE_TOO_LOW(-2),
        INVALID(-1),

        ACCEPTED(1),
        KNOWN(2),
    }

    companion object {
        const val TX_RELAY_FEE_MIN: Satoshi = 1UL
        const val TXS_PER_SENDER_MAX = 500
        const val FREE_TX_PER_SENDER_MAX = 10
        const val SIZE_MAX = 100000
    }

    val txByFeePerByte = TreeSet<Transaction>()
    val txByHash = HashMap<HashLight, Transaction>()
    val txSetBySender = HashMap<Address, MempoolTransactionSet>()
    val txSetByRecipient = HashMap<Address, HashSet<HashLight>>()

    fun pushTransaction(tx: Transaction): ReturnCode {
        // Check if we already know this transaction.
        if (txByHash.contains(tx.hash))
            return ReturnCode.KNOWN

        val set = txSetBySender[tx.sender] ?: MempoolTransactionSet()
        // Check limit for free transactions.
        if (tx.feePerByte < TX_RELAY_FEE_MIN
            && set.numBelowFeePerByte(TX_RELAY_FEE_MIN) >= FREE_TX_PER_SENDER_MAX)
            return ReturnCode.FEE_TOO_LOW

        // Intrinsic transaction verification
        if (!tx.verify())
            return ReturnCode.INVALID

        // Retrieve recipient account and test incoming transaction.
        val recipientAccount: Account
        try {
            recipientAccount = accounts.get(tx.recipient)
            recipientAccount.withIncomingTransaction(tx, blockchain.height + 1)
        } catch (e: Exception) {
            return Mempool.ReturnCode.INVALID
        }

        // Retrieve sender account.
        val senderAccount: Account
        try {
            senderAccount = accounts.get(tx.sender, tx.senderType)
        } catch (e: Exception) {
            return Mempool.ReturnCode.INVALID
        }

        // Add new transaction to the sender's pending transaction set. Then re-check all transactions in the set
        // in fee/byte order against the sender account state. Adding high fee transactions may thus invalidate
        // low fee transactions in the set.
        val txs = ArrayList<Transaction>()
        val txsToRemove = ArrayList<Transaction>()
        var tmpAccount = senderAccount
        for (tmpTx in set.copyAndAdd(tx).txs) {
            try {
                if (txs.size < TXS_PER_SENDER_MAX) {
                    tmpAccount = tmpAccount.withOutgoingTransaction(
                        transaction = tmpTx,
                        blockHeight = blockchain.height + 1,
                        txCache = blockchain.transactionCache
                    )
                    txs.add(tmpTx)
                } else throw IllegalStateException("transactions per sender exceeded")
            } catch (e: Exception) {
                // An error occurred processing this transaction.
                // If the rejected transaction is the one we're pushing, fail.
                // Otherwise, evict the rejected transaction from the mempool.
                if (tx == tmpTx) {
                    return Mempool.ReturnCode.INVALID
                } else {
                    txsToRemove.add(tx)
                }
            }
        }

        // Remove invalidated transactions.
        for (tmpTx in txsToRemove)
            removeTransaction(tmpTx)

        // Transaction is valid, add it to the mempool.
        txByFeePerByte += tx
        txByHash[tx.hash] = tx
        txSetBySender[tx.sender] = MempoolTransactionSet(txs.toSortedSet())
        txSetByRecipient[tx.recipient] =
            (txSetByRecipient[tx.recipient] ?: HashSet())
            .apply { add(tx.hash) }
        if (txByFeePerByte.size > SIZE_MAX)
            popLowFeeTxs()

        return ReturnCode.ACCEPTED
    }

    private fun popLowFeeTxs() {
        // Remove transaction
        val tx = txByFeePerByte.last()
        removeTransaction(tx)
        txSetBySender[tx.sender]?.remove(tx)
    }

    // Does *not* remove transaction from transactionsBySender!
    private fun removeTransaction(tx: Transaction) {
        txByHash.remove(tx.hash)
        txByFeePerByte.remove(tx)
        // Force get txSetByRecipient
        (txSetByRecipient[tx.recipient]
            ?: throw IllegalStateException("no txSetByRecipient for $tx"))
            .apply {
                // Remove set list or tx
                if (size == 1)
                    txSetByRecipient.remove(tx.recipient)
                else
                    remove(tx.hash)
            }
    }

    operator fun get(hash: HashLight) = txByHash[hash]




}
