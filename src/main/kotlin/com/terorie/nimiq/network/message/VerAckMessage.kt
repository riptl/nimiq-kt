package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class VerAckMessage(
    val publicKey: PublicKeyNim,
    val signatureNim: SignatureNim
) : Message(type) {

    companion object : MessageEnc<VerAckMessage>() {
        override val type = Message.Type.VERACK

        override fun serializedContentSize(m: VerAckMessage) =
            PublicKeyNim.SIZE + SignatureNim.SIZE

        override fun deserializeContent(s: InputStream, h: Header) = VerAckMessage(
            s.read(PublicKeyNim()),
            s.read(SignatureNim())
        )

        override fun serializeContent(s: OutputStream, m: VerAckMessage) = with(m) {
            s.write(m.publicKey)
            s.write(m.signatureNim)
        }
    }

}
