package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.consensus.blockchain.IBlockchain
import com.terorie.nimiq.consensus.mempool.Mempool
import com.terorie.nimiq.consensus.mempool.MempoolTransactionSet
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.Transaction

@ExperimentalUnsignedTypes
class NanoMempool(private val blockchain: IBlockchain) : Mempool(blockchain) {

    val txByHash = HashMap<HashLight, Transaction>()
    val txSetByAddress = HashMap<Address, MempoolTransactionSet>()

    fun pushTransaction(tx: Transaction): Boolean {
        // Check if we already know this transaction.
        if (txByHash.contains(tx.hash)) {
            // TODO log
            return false
        }

        // Check validity based on startHeight
        if (blockchain.height >= tx.validityStartHeight) {
            // TODO log
            return false
        }

        // Verify transaction.
        if (!tx.verify())
            return false

        // Transaction is valid, add it to the mempool.
        txByHash[tx.hash] = tx
        val set = txSetByAddress[tx.sender] ?: MempoolTransactionSet()
        set.add(tx)
        txSetByAddress.put(tx.sender, set)

        // TODO Tell listeners about the new transaction we received.

        return true
    }

    fun getTransaction(hash: HashLight) = txByHash[hash]

    fun getTransactions(maxCount: Int = 5000) =
        txByHash.values.sorted().slice(0..maxCount)

    fun getPendingTransactions(address: Address) =
        txSetByAddress[address]?.txs?.toList() ?: emptyList()

    fun changeHead(block: Block, txs: List<Transaction>) {
        evictTransactions(block.header, txs)
    }

    fun removeTransaction(tx: Transaction) {
        txByHash.remove(tx.hash)

        val set = txSetByAddress[tx.sender]!!
        set.remove(tx)

        if (set.txs.isEmpty())
            txSetByAddress.remove(tx.sender, set)

        // TODO fire transaction-removed
    }

    fun evictExceptAddresses(addresses: Set<Address>) {
        for (tx in txByHash.values)
            if (!addresses.contains(tx.sender))
                removeTransaction(tx)
    }

    // Remove specified mined transactions and
    // garbage collect expired transactions
    private fun evictTransactions(blockHeader: BlockHeader, txs: List<Transaction>) {
        // Remove expired transactions.
        for (tx in txByHash.values) {
            if (blockHeader.height >= tx.validityStartHeight + Policy.TRANSACTION_VALIDITY_WINDOW) {
                removeTransaction(tx)
                // TODO fire transaction-expired
            }
        }

        // Remove mined transactions.
        for (tx in txs) {
            if (txByHash.contains(tx.hash)) {
                removeTransaction(tx)
                // TODO fire transaction-mined
            }
        }
    }

}
