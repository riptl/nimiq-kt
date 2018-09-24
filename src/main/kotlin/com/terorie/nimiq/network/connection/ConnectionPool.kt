package com.terorie.nimiq.network.connection

import com.terorie.nimiq.network.NetworkConfig
import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.network.address.PeerAddressBook

@ExperimentalUnsignedTypes
class ConnectionPool(
        val peerAddresses: PeerAddressBook,
        val networkConfig: NetworkConfig,
        val blockchain: Blockchain,
        val time: UInt
) {

    companion object {
        const val DEFAULT_BAN_TIME = 1000 * 60 * 10 // 10 minutes
        const val UNBAN_IPS_INTERVAL = 1000 * 60 // 1 minute
    }

    private val connectionsByPeerAddress = HashMap<PeerAddress, PeerConnection>()

    private var _peerCountOutbound = 0
    val peerCountOutbound: Int
        get() = _peerCountOutbound

    private var _peerCountFullWsOutbound = 0
    val peerCountFullWsOutbound: Int
        get() = _peerCountFullWsOutbound

    var allowInboundConnections = false

    fun disconnect(reason: String?) {
        // Close all active connections
        for ((_, connection) in connectionsByPeerAddress) {
            connection.peerChannel.close(CloseType.MANUAL_NETWORK_DISCONNECT, reason ?: "manual network disconnect")
        }
    }

}
