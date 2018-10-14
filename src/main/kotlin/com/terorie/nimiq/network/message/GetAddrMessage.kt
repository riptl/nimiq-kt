package com.terorie.nimiq.network.message

import com.terorie.nimiq.network.connection.NetworkAgent
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class GetAddrMessage(
    val protocolMask: UByte,
    val serviceMask: UInt,
    val maxResults: UShort
) : Message(type) {

    companion object : MessageEnc<GetAddrMessage>() {
        override val type = Message.Type.GET_ADDR

        override fun serializedContentSize(m: GetAddrMessage): Int =
            7

        override fun deserializeContent(s: InputStream, h: Header): GetAddrMessage {
            val protocolMask = s.readUByte()
            val serviceMask = s.readUInt()
            var maxResults = NetworkAgent.NUM_ADDR_PER_REQUEST.toUShort()
            if (s.available() > 0)
                maxResults = s.readUShort()
            return GetAddrMessage(protocolMask, serviceMask, maxResults)
        }

        override fun serializeContent(s: OutputStream, m: GetAddrMessage) = with(m) {
            s.writeUByte(protocolMask)
            s.writeUInt(serviceMask)
            s.writeUShort(maxResults)
        }
    }

}
