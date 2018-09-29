package com.terorie.nimiq.consensus.block

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.account.PrunedAccount
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.util.MerkleTree
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class BlockBody(
        val minerAddr: Address,
        val transactions: ArrayList<Transaction>,
        val extraData: ByteArray,
        val prunedAccounts: ArrayList<PrunedAccount> = arrayListOf()
) {

    companion object : Enc<BlockBody> {
        override fun serializedSize(o: BlockBody): Int = with(o) {
            var size = 25 // Static fields
            size += extraData.size
            for (tx in transactions)
                size += tx.serializedSize
            return size
        }

        override fun deserialize(s: InputStream): BlockBody {
            val minerAddr = s.read(Address())
            val extraDataLen = s.readUByte()
            val extraData = s.readFull(extraDataLen.toInt())
            val numTxs = s.readUShort().toInt()
            val txs = ArrayList<Transaction>(numTxs)
            for (i in 0 until numTxs)
                txs[i] = Transaction.deserialize(s)
            val numPrunedAccs = s.readUShort().toInt()
            val prunedAccs = ArrayList<PrunedAccount>()
            for (i in 0 until numPrunedAccs)
                prunedAccs[i] = s.read(PrunedAccount)

            return BlockBody(minerAddr, txs, extraData, prunedAccs)
        }

        override fun serialize(s: OutputStream, o: BlockBody) = with(o) {
            s.write(minerAddr)
            s.writeUByte(extraData.size)
            s.write(extraData)
            s.writeUShort(transactions.size)
            transactions.forEach { Transaction.serialize(s, it) }
            s.writeUShort(prunedAccounts.size)
            prunedAccounts.forEach{ PrunedAccount.serialize(s, it) }
        }
    }

    fun verify(): Boolean {
        var previousTx: Transaction? = null
        for (tx in transactions) {
            // Ensure transactions are ordered and unique.
            if (previousTx != null && previousTx >= tx) {
                return false
            }
            previousTx = tx

            // Check that all transactions are valid
            if (!tx.verify()) {
                return false
            }
        }

        var previousAcc: PrunedAccount? = null
        for (acc in prunedAccounts) {
            // Ensure pruned accounts are ordered and unique.
            if (previousAcc != null && previousAcc >= acc) {
                return false
            }
            previousAcc = acc

            // Check that pruned accounts are actually supposed to be pruned
            if (!acc.account.isToBePruned()) {
                return false
            }
        }

        // Everything checks out.
        return true
    }

    private var _hash: HashLight? = null
    val hash: HashLight
        get() {
        if (_hash == null)
            _hash = MerkleTree.computeRoot(getMerkleLeafs())
        return _hash!!
    }

    private fun getMerkleLeafs() = ArrayList<HashLight>().apply {
        add(minerAddr.hash)
        add(HashLight(extraData))
        addAll(transactions.map { it.hash })
        addAll(prunedAccounts.map { it.hash })
    }

    fun getAddresses() = ArrayList<Address>().apply {
        add(minerAddr)
        for (tx in transactions) {
            add(tx.sender)
            add(tx.recipient)
        }
    }

}
