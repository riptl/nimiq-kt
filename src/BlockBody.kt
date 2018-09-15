import java.io.InputStream
import java.io.OutputStream

class BlockBody(
        val minerAddr: Address,
        val transactions: ArrayList<Transaction>,
        val extraData: ByteArray,
        val prunedAccounts: ArrayList<PrunedAccount>
) {

    companion object {
        fun unserialize(s: InputStream): BlockBody {
            val minerAddr = Address().apply{ unserialize(s) }
            val extraDataLen = s.readUByte()
            val extraData = s.readFull(extraDataLen)
            val numTxs = s.readUShort()
            val txs = ArrayList<Transaction>(numTxs)
            for (i in 0 until numTxs)
                txs[i] = Transaction.unserialize(s)
            val numPrunedAccs = s.readUShort()
            val prunedAccs = ArrayList<PrunedAccount>()
            for (i in 0 until numPrunedAccs)
                prunedAccs[i] = PrunedAccount.unserialize(s)

            return BlockBody(minerAddr, txs, extraData, prunedAccs)
        }
    }

    fun verify(): Boolean {
        var previousTx: Transaction? = null
        for (tx in transactions) {
            // Ensure transactions are ordered and unique.
            if (previousTx != null && previousTx.compareBlockOrder(tx) >= 0) {
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
            if (previousAcc != null && previousAcc > acc) {
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

    fun serialize(s: OutputStream) {
        minerAddr.serialize(s)
        s.writeUByte(extraData.size)
        s.write(extraData)
        s.writeUShort(transactions.size)
        transactions.forEach { it.serialize(s) }
        s.writeUShort(prunedAccounts.size)
        prunedAccounts.forEach{ it.serialize(s) }
    }

}