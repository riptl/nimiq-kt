package com.terorie.nimiq

import java.net.InetSocketAddress

@ExperimentalUnsignedTypes
open class PeerAddress(
    val protocol: Protocol,
    val services: UInt,
    val timestamp: UInt,
    val netAddress: InetSocketAddress,
    val publicKey: PublicKeyNim?,
    val signature: SignatureNim?
)

@ExperimentalUnsignedTypes
open class WsBasePeerAddress(
    protocol: Protocol,
    services: UInt,
    timestamp: UInt,
    netAddress: InetSocketAddress,
    publicKey: PublicKeyNim?,
    val distance: Int,
    val host: String,
    val port: Int,
    signature: SignatureNim?
) : PeerAddress(protocol, services, timestamp, netAddress, publicKey, signature)

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
) : WsBasePeerAddress(Protocol.WSS, services, timestamp, netAddress, publicKey, distance, host, port, signature) {

    companion object {
        fun seed(host: String, port: Int, pubKeyHex: String?): WssPeerAddress {
            val pubKey: PublicKeyNim? =
                if (pubKeyHex != null)
                    PublicKeyNim.fromHex(pubKeyHex)
                else null
            return WssPeerAddress(
                services = Services.FULL,
                timestamp = 0U,
                netAddress = InetSocketAddress(host, port),
                publicKey = pubKey,
                distance = 0,
                host = host, port = port
            )
        }
    }

}
