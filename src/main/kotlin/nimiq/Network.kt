package com.terorie.nimiq

@ExperimentalUnsignedTypes
class Network(
        val blockchain: Blockchain,
        val networkConfig: NetworkConfig,
        val time: UInt
) {

    companion object {
        const val PEER_COUNT_MAX = 4000
        const val INBOUND_PEER_COUNT_PER_SUBNET_MAX = 100
        const val OUTBOUND_PEER_COUNT_PER_SUBNET_MAX = 2
        const val PEER_COUNT_PER_IP_MAX = 20
        const val PEER_COUNT_DUMB_MAX = 1000
        const val IPV4_SUBNET_MASK = 24
        const val IPV6_SUBNET_MASK = 96
        const val PEER_COUNT_RECYCLING_ACTIVE = 1000
        const val RECYCLING_PERCENTAGE_MIN = 0.01
        const val RECYCLING_PERCENTAGE_MAX = 0.20
        const val CONNECTING_COUNT_MAX = 2
        const val SIGNAL_TTL_INITIAL = 3
        const val CONNECT_BACKOFF_INITIAL = 2000 // 2 seconds
        const val CONNECT_BACKOFF_MAX = 10 * 60 * 1000 // 10 minutes
        const val TIME_OFFSET_MAX = 15 * 60 * 1000 // 15 minutes
        const val HOUSEKEEPING_INTERVAL = 5 * 60 * 1000 // 5 minutes
        const val SCORE_INBOUND_EXCHANGE = 0.5
        const val CONNECT_THROTTLE = 1000 // 1 second
        const val ADDRESS_REQUEST_CUTOFF = 250
        const val ADDRESS_REQUEST_PEERS = 2
        const val SIGNALING_ENABLED = 1
    }

    private var autoConnect = false
    private var backOff = CONNECT_BACKOFF_INITIAL
    private var backedOff = false
    private var addresses = PeerAddressBook(networkConfig)
    private var connections = ConnectionPool(addresses, networkConfig, blockchain, time)
    private var scorer = PeerScorer(networkConfig, addresses, connections)

    fun connect() {
        autoConnect = true
        // TODO Housekeeping

        // Start connecting to peers
        checkPeerCount()
    }

    fun disconnect(reason: String) {
        autoConnect = false
        // TODO Housekeeping

        connections.disconnect(reason)
        connections.allowInboundConnections = false
    }

    private fun checkPeerCount() {
        if (autoConnect /* && TODO */) {
            // Pick a peer address that we are not connected to yet.

        }
    }

}
