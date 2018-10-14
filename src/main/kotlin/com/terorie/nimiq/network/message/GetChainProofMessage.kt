package com.terorie.nimiq.network.message

import java.io.InputStream

@ExperimentalUnsignedTypes
class GetChainProofMessage : Message(type) {

    companion object : EmptyMessageEnc<GetChainProofMessage>() {
        override val type = Message.Type.GET_CHAIN_PROOF
        override fun deserializeContent(s: InputStream, h: Header) = GetChainProofMessage()
    }

}
