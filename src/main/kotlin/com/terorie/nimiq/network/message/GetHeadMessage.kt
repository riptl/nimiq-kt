package com.terorie.nimiq.network.message

import java.io.InputStream

@ExperimentalUnsignedTypes
class GetHeadMessage : Message(type) {

    companion object : EmptyMessageEnc<GetHeadMessage>() {
        override val type = Message.Type.GET_CHAIN_PROOF
        override fun deserializeContent(s: InputStream, h: Header) = GetHeadMessage()
    }

}
