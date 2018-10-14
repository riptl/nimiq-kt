package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class GetBlockProofMessage(
    val blockHashToProve: HashLight,
    val knownBlockHash: HashLight
) : Message(type) {

    companion object : MessageEnc<GetBlockProofMessage>() {
        override val type = Message.Type.GET_BLOCK_PROOF

        override fun serializedContentSize(m: GetBlockProofMessage): Int =
            2 * HashLight.SIZE

        override fun deserializeContent(s: InputStream, h: Header) = GetBlockProofMessage(
            blockHashToProve = s.read(HashLight()),
            knownBlockHash = s.read(HashLight())
        )

        override fun serializeContent(s: OutputStream, m: GetBlockProofMessage) = with(m) {
            s.write(blockHashToProve)
            s.write(knownBlockHash)
        }
    }

}
