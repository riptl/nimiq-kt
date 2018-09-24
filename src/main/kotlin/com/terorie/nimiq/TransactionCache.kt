package com.terorie.nimiq

@ExperimentalUnsignedTypes
class TransactionCache {

    private val txHashes = HashSet<HashLight>()

    fun contains(tx: Transaction) =
        txHashes.contains(tx.hash)

}
