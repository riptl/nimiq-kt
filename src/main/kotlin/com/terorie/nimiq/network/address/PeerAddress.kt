package com.terorie.nimiq.network.address

import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.network.Protocol
import com.terorie.nimiq.util.Services
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress

@ExperimentalUnsignedTypes
open class PeerAddress(
        val protocol: Protocol,
        val services: UInt,
        val timestamp: UInt,
        val netAddress: InetSocketAddress,
        val publicKey: PublicKeyNim?,
        var distance: Int,
        val signature: SignatureNim?
) {

    companion object : Enc<PeerAddress> {
        override fun deserialize(s: InputStream): PeerAddress {
            TODO("not implemented")
        }

        override fun serialize(s: OutputStream, o: PeerAddress) {
            TODO("not implemented")
        }

        override fun serializedSize(o: PeerAddress): Int {
            TODO("not implemented")
        }
    }

}

@ExperimentalUnsignedTypes
open class WsBasePeerAddress(
        protocol: Protocol,
        services: UInt,
        timestamp: UInt,
        netAddress: InetSocketAddress,
        publicKey: PublicKeyNim?,
        distance: Int,
        val host: String,
        val port: Int,
        signature: SignatureNim?
) : PeerAddress(protocol, services, timestamp, netAddress, publicKey, distance, signature) {

    companion object {
        fun seed(protocol: Protocol, host: String, port: Int, pubKeyHex: String?): WsBasePeerAddress {
            val pubKey: PublicKeyNim? =
                if (pubKeyHex != null)
                    PublicKeyNim.fromHex(pubKeyHex)
                else null

            return when (protocol) {
                Protocol.WSS -> WssPeerAddress(
                        services = Services.FULL,
                        timestamp = 0U,
                        netAddress = InetSocketAddress(host, port),
                        publicKey = pubKey,
                        distance = 0,
                        host = host, port = port
                )
                Protocol.WS -> WsPeerAddress(
                        services = Services.FULL,
                        timestamp = 0U,
                        netAddress = InetSocketAddress(host, port),
                        publicKey = pubKey,
                        distance = 0,
                        host = host, port = port
                )
                else -> throw IllegalArgumentException("not a WebSockets peer")
            }
        }
    }

}

@ExperimentalUnsignedTypes
class WsPeerAddress(
        services: UInt,
        timestamp: UInt,
        netAddress: InetSocketAddress,
        publicKey: PublicKeyNim?,
        distance: Int,
        host: String,
        port: Int,
        signature: SignatureNim? = null
) : WsBasePeerAddress(Protocol.WS, services, timestamp, netAddress, publicKey, distance, host, port, signature)

@ExperimentalUnsignedTypes
class WssPeerAddress(
        services: UInt,
        timestamp: UInt,
        netAddress: InetSocketAddress,
        publicKey: PublicKeyNim?,
        distance: Int,
        host: String,
        port: Int,
        signature: SignatureNim? = null
) : WsBasePeerAddress(Protocol.WSS, services, timestamp, netAddress, publicKey, distance, host, port, signature)
