package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.blockchain.BaseChain
import com.terorie.nimiq.network.Network
import com.terorie.nimiq.network.Peer
import com.terorie.nimiq.util.Services

@ExperimentalUnsignedTypes
open class BaseConsensus(
        val blockchain: BaseChain,
        val mempool: Mempool,
        val network: Network
) {

    private val agents = HashMap<Peer, BaseConsensusAgent>()
    private var established = false
    private var syncPeer: Peer? = null
    private var subscription: Subscription = SubscriptionAny
    private val invRequestManager = InvRequestManager()

    private fun syncBlockchain() {
        //val candidates = ArrayList<>
        var numSyncedFullNodes = 0
        for (agent in agents.values) {
            if (!agent.synced)
                candidates.add(agent)
            else if (Services.isFullNode(agent.peer.peerAddress.services))
                numSyncedFullNodes++
        }

        // TODO Report consensus-lost if we are synced with less than the minimum number of full nodes or have no connections at all.

        // Wait for ongoing sync to finish
        if (syncPeer != null)
            return

        // TODO Choose a random peer which we aren't sync'd with yet.

        syncPeer = agent.peer

        // TODO Notify listeners when we start syncing and have not established consensus yet.

        // TODO Log Syncing blockchain
        agent.syncBlockchain()
    }

}