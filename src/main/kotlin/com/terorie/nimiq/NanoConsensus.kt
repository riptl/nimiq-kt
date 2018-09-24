package com.terorie.nimiq

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