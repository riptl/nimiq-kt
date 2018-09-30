package com.terorie.nimiq.network.message

import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class AddrMessage(val addresses: Array<PeerAddress>) : Message(type) {

    companion object : MessageEnc<AddrMessage>() {
        override val type = Message.Type.ADDR

        override fun serializedContentSize(m: AddrMessage) =
            2 + m.addresses.sumBy { PeerAddress.serializedSize(it) }

        override fun deserializeContent(s: InputStream, h: Header) = AddrMessage(
            Array(s.readUShort().toInt()) { s.read(PeerAddress) }
        )

        override fun serializeContent(s: OutputStream, m: AddrMessage) = with(m) {
            s.writeUShort(addresses.size)
            for (address in addresses)
                s.write(PeerAddress, address)
        }
    }

}
