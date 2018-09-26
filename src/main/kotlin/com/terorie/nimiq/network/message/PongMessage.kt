package com.terorie.nimiq.network.message

import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class PongMessage(val nonce: UInt) : Message(Message.Type.PING) {

    companion object : MessageEnc<PongMessage> {
        override fun serializedContentSize(m: PongMessage) = 4
        override fun deserializeContent(s: InputStream, h: MessageEnc.Header) =
            PongMessage(s.readUInt())
        override fun serializeContent(s: OutputStream, m: PongMessage) =
            s.writeUInt(m.nonce)
    }

}
