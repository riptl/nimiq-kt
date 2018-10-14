package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class GetAccountsTreeChunkMessage(
    val blockHash: HashLight,
    val startPrefix: String
) : Message(type) {

    companion object : MessageEnc<GetAccountsTreeChunkMessage>() {
        override val type = Message.Type.GET_ACCOUNTS_TREE_CHUNK

        override fun serializedContentSize(m: GetAccountsTreeChunkMessage): Int =
            HashLight.SIZE + varStringSize(m.startPrefix)

        override fun deserializeContent(s: InputStream, h: Header) = GetAccountsTreeChunkMessage(
            blockHash = s.read(HashLight()),
            startPrefix = s.readVarString()
        )

        override fun serializeContent(s: OutputStream, m: GetAccountsTreeChunkMessage) = with(m) {
            s.write(blockHash)
            s.writeVarString(startPrefix)
        }
    }

}
