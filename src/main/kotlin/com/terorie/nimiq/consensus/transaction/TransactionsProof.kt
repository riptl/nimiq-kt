package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.MerkleProof
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class TransactionsProof(
    val txs: MutableList<Transaction>,
    val proof: MerkleProof
) {

    companion object : Enc<TransactionsProof> {
        override fun serializedSize(o: TransactionsProof): Int {
            var x = 2
            for (tx in o.txs)
                x += Transaction.serializedSize(tx)
            x += MerkleProof.serializedSize(o.proof)
            return x
        }

        override fun deserialize(s: InputStream) = TransactionsProof(
            txs = Array(s.readUShort().toInt()) {
                s.read(Transaction)
            }.toMutableList(),
            proof = s.read(MerkleProof)
        )

        override fun serialize(s: OutputStream, o: TransactionsProof) = with(o) {
            s.writeUShort(txs.size)
            for (tx in txs)
                s.write(Transaction, tx)
            s.write(MerkleProof, proof)
        }
    }

    fun root(): HashLight = proof.computeRoot(txs.map { it.hash })

}
