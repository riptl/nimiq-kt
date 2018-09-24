package com.terorie.nimiq.consensus.mempool

import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.transaction.Transaction
import java.util.*

@ExperimentalUnsignedTypes
class MempoolTransactionSet(val txs: SortedSet<Transaction>) {

    fun add(tx: Transaction) = txs.add(tx)
    fun remove(tx: Transaction) = txs.remove(tx)

    fun copyAndAdd(tx: Transaction) =
        MempoolTransactionSet(txs.toSortedSet())
            .apply { add(tx) }

    val sender: Address?
        get() = txs.firstOrNull()?.sender

    val senderType: Account.Type?
        get() = txs.firstOrNull()?.senderType

    val length get() = txs.size

    fun numBelowFeePerByte(feePerByte: Satoshi): Int {
        var count = 0
        for (tx in txs)
            if (tx.feePerByte < feePerByte)
                count++
            else
                break // Sorted by feePerByte
        return count
    }

}
