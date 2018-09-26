package com.terorie.nimiq.network.message

import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class PingMessage(val nonce: UInt) : Message(Message.Type.PING) {

    companion object : MessageEnc<PingMessage> {
        override fun serializedContentSize(m: PingMessage) = 4
        override fun deserializeContent(s: InputStream, h: MessageEnc.Header) =
            PingMessage(s.readUInt())
        override fun serializeContent(s: OutputStream, m: PingMessage) =
            s.writeUInt(m.nonce)
    }

}
