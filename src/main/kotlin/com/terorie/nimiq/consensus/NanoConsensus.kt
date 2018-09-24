package com.terorie.nimiq.consensus

import com.terorie.nimiq.network.Network
import com.terorie.nimiq.network.Peer

@ExperimentalUnsignedTypes
class NanoConsensus(
        blockchain: NanoChain,
        mempool: NanoMempool,
        network: Network
) : BaseConsensus(blockchain, mempool, network) {

    private var established = false
    private var syncPeer: Peer? = null
    private var subscription: Subscription = SubscriptionAny
    private val invRequestManager = InvRequestManager()

}