package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class BlockMessage(val block: Block) : Message(type) {

    companion object : MessageEnc<BlockMessage>() {
        override val type = Message.Type.BLOCK

        override fun serializedContentSize(m: BlockMessage) =
            Block.serializedSize(m.block)

        override fun deserializeContent(s: InputStream, h: Header) =
            BlockMessage(s.read(Block))

        override fun serializeContent(s: OutputStream, m: BlockMessage) =
            s.write(Block, m.block)
    }

}
