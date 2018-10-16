package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.TransactionsProof
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class TransactionsProofMessage(
    val blockHash: HashLight,
    val proof: TransactionsProof? = null
) : Message(type) {

    companion object : MessageEnc<TransactionsProofMessage>() {
        override val type = Message.Type.TRANSACTIONS_PROOF

        override fun serializedContentSize(m: TransactionsProofMessage): Int {
            var x = 1 + HashLight.SIZE
            if (m.proof != null)
                x += TransactionsProof.serializedSize(m.proof!!)
            return x
        }

        override fun deserializeContent(s: InputStream, h: Header): TransactionsProofMessage {
            val blockHash = s.read(HashLight())
            val hasProof = s.readBool()
            var proof: TransactionsProof? = null
            if (hasProof)
                proof = s.read(TransactionsProof)
            return TransactionsProofMessage(blockHash, proof)
        }

        override fun serializeContent(s: OutputStream, m: TransactionsProofMessage) = with(m) {
            s.write(blockHash)
            if (m.proof != null)
                s.write(TransactionsProof, proof!!)
        }
    }

}
