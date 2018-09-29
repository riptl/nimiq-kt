package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.blockchain.ChainProof
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class ChainProofMessage(val proof: ChainProof) : Message(type) {

    companion object : MessageEnc<ChainProofMessage>() {
        override val type: Type = Message.Type.CHAIN_PROOF

        override fun serializedContentSize(m: ChainProofMessage): Int =
            ChainProof.serializedSize(m.proof)

        override fun deserializeContent(s: InputStream, h: Header) =
            ChainProofMessage(s.read(ChainProof))

        override fun serializeContent(s: OutputStream, m: ChainProofMessage) =
            s.write(ChainProof, m.proof)
    }

}
