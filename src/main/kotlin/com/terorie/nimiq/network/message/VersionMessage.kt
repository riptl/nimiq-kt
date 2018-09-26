package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.util.Blob
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class VersionMessage(
        val version: UInt,
        val peerAddress: PeerAddress,
        val genesisHash: HashLight,
        val headHash: HashLight,
        val challengeNonce: ChallengeNonce
) : Message(type) {

    companion object : MessageEnc<VersionMessage>() {
        override val type = Message.Type.VERSION

        override fun deserializeContent(s: InputStream, h: MessageEnc.Header) = VersionMessage(
            version = s.readUInt(),
            peerAddress = PeerAddress.deserialize(s),
            genesisHash = s.read(HashLight()),
            headHash = s.read(HashLight()),
            challengeNonce = s.read(ChallengeNonce())
        )

        override fun serializeContent(s: OutputStream, m: VersionMessage) = with(m) {
            s.writeUInt(version)
            s.write(PeerAddress, peerAddress)
            s.write(genesisHash)
            s.write(headHash)
            s.write(challengeNonce)
        }

        override fun serializedContentSize(m: VersionMessage) =
            100 + PeerAddress.serializedSize(m.peerAddress)
    }

    class ChallengeNonce : Blob(32)

}
