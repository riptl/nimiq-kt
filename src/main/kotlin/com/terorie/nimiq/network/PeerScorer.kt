package com.terorie.nimiq.network

import com.terorie.nimiq.network.address.PeerAddressBook
import com.terorie.nimiq.network.connection.ConnectionPool

@ExperimentalUnsignedTypes
class PeerScorer(
        val networkConfig: NetworkConfig,
        val addressBook: PeerAddressBook,
        val connections: ConnectionPool
) {

    companion object {
        const val PEER_COUNT_MIN_FULL_WS_OUTBOUND = 12
        const val PEER_COUNT_MIN_OUTBOUND = 12
        const val PICK_SELECTION_SIZE = 100
        const val MIN_AGE_FULL = 5 * 60 * 1000 // 5 minutes
        const val BEST_AGE_FULL = 24 * 60 * 60 * 1000 // 24 hours
        const val MIN_AGE_LIGHT = 2 * 60 * 1000 // 2 minutes
        const val BEST_AGE_LIGHT = 15 * 60 * 1000 // 15 minutes
        const val MAX_AGE_LIGHT = 6 * 60 * 60 * 1000 // 6 hours
        const val MIN_AGE_NANO = 60 * 1000 // 1 minute
        const val BEST_AGE_NANO = 5 * 60 * 1000 // 5 minutes
        const val MAX_AGE_NANO = 30 * 60 * 1000 // 30 minutes
        const val BEST_PROTOCOL_WS_DISTRIBUTION = 0.15 // 15%
    }

    val needsGoodPeers: Boolean
        get() = connections.peerCountFullWsOutbound < PEER_COUNT_MIN_FULL_WS_OUTBOUND

    val needsMorePeers: Boolean
        get() = connections.peerCountOutbound < PEER_COUNT_MIN_OUTBOUND

}
