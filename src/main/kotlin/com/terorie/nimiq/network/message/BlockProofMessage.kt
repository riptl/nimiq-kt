package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.blockchain.BlockChain
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class BlockProofMessage(
    val proof: BlockChain? = null
) : Message(type) {

    companion object : MessageEnc<BlockProofMessage>() {
        override val type = Message.Type.BLOCK_PROOF

        override fun serializedContentSize(m: BlockProofMessage): Int {
            return if (m.proof != null)
                1 + BlockChain.serializedSize(m.proof)
            else
                0
        }

        override fun deserializeContent(s: InputStream, h: Header): BlockProofMessage {
            val hasProof = s.readBool()
            return if (hasProof)
                BlockProofMessage(s.read(BlockChain))
            else
                BlockProofMessage()
        }

        override fun serializeContent(s: OutputStream, m: BlockProofMessage) = with(m) {
            val hasProof = proof != null
            s.writeBool(hasProof)
            if (hasProof)
                s.write(BlockChain, proof!!)
        }
    }

}
