package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.primitive.HashLight

@ExperimentalUnsignedTypes
class TransactionCache {

    private val txHashes = HashSet<HashLight>()

    fun contains(tx: Transaction) =
        txHashes.contains(tx.hash)

}
