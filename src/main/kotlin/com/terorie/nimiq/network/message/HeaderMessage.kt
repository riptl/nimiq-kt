package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class HeaderMessage(val header: BlockHeader) : Message(type) {

    companion object : MessageEnc<HeaderMessage>() {
        override val type = Message.Type.HEADER

        override fun serializedContentSize(m: HeaderMessage) =
            BlockHeader.serializedSize(m.header)

        override fun deserializeContent(s: InputStream, h: Header) =
            HeaderMessage(s.read(BlockHeader))

        override fun serializeContent(s: OutputStream, m: HeaderMessage) =
            s.write(BlockHeader, m.header)
    }

}
