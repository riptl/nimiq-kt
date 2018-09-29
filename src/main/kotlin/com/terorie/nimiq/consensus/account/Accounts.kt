package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.transaction.TransactionCache
import java.lang.Exception
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
class Accounts(private val accountsTree: AccountsTree) {

    fun getAccountsProof(addresses: List<Address>) =
        accountsTree.getAccountsProof(addresses)

    fun getAccountsTreeChunk(startPrefix: String) =
        accountsTree.getChunk(startPrefix)

    fun commitBlock(block: Block, txCache: TransactionCache) {
        block.body!!
        val tree = accountsTree.synchronousTransaction()
        tree.preloadAddresses(block.body.getAddresses())
        try {
            commitBlockBody(tree, block.body, block.header.height, txCache)
        } catch (e: Exception) {
            tree.abort()
            throw e
        }

        tree.finalizeBatch()

        val hash = tree.rootSync()
        if (block.header.accountsHash != hash) {
            tree.abort()
            throw IllegalArgumentException("AccountsHash mismatch")
        }

        tree.commit()
    }

}
