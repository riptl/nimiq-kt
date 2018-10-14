package com.terorie.nimiq.network.connection

import com.terorie.nimiq.consensus.blockchain.IBlockchain
import com.terorie.nimiq.network.NetworkConfig
import com.terorie.nimiq.network.address.PeerAddressBook

// TODO Implement
@ExperimentalUnsignedTypes
class NetworkAgent(
    val blockchain: IBlockchain,
    val addresses: PeerAddressBook,
    val networkConfig: NetworkConfig,
    val channel: PeerChannel
) {

    companion object {
        const val HANDSHAKE_TIMEOUT = 1000 * 4 // 4 seconds
        const val PING_TIMEOUT = 1000 * 10 // 10 seconds
        const val CONNECTIVITY_CHECK_INTERVAL = 1000 * 60 // 1 minute
        const val ANNOUNCE_ADDR_INTERVAL = 1000 * 60 * 10 // 10 minutes
        const val VERSION_ATTEMPTS_MAX = 10
        const val VERSION_RETRY_DELAY = 500 // 500 ms
        const val GETADDR_RATE_LIMIT = 3 // per minute
        const val MAX_ADDR_PER_MESSAGE = 1000
        const val MAX_ADDR_PER_REQUEST = 500
        const val NUM_ADDR_PER_REQUEST = 200
    }

}
