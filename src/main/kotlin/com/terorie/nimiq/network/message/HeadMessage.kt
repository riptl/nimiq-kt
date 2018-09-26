package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class HeadMessage(val header: BlockHeader) : Message(Message.Type.GET_HEAD) {

    companion object : MessageEnc<HeadMessage>() {
        override val type = Message.Type.HEAD

        // Size of header
        override fun serializedContentSize(m: HeadMessage) = 146

        override fun deserializeContent(s: InputStream, h: Header) =
            HeadMessage(s.read(BlockHeader))

        override fun serializeContent(s: OutputStream, m: HeadMessage) =
            s.write(BlockHeader, m.header)
    }

}
