package com.terorie.nimiq.network.connection

import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.network.address.PeerAddressBook

@ExperimentalUnsignedTypes
class SignalRouter(val peerAddress: PeerAddress) {

    private var bestRoute: SignalRoute? = null
    private val routes = HashSet<SignalRoute>()

    fun addRoute(signalChannel: PeerChannel, distance: Int, timestamp: UInt): Boolean {
        val existed = routes.contains(signalChannel)
        val newRoute = SignalRoute(signalChannel, distance, timestamp)

        if (existed) {
            // Do not reset failed attempts.
            newRoute.failedAttempts = oldRoute.failedAttempts
        }
        routes.add(newRoute)

        if (bestRoute == null
            || newRoute.score > bestRoute!!.score
            || (newRoute.score == bestRoute!!.score
                && timestamp > bestRoute!!.timestamp)) {
            bestRoute = newRoute
            peerAddress.distance = bestRoute!!.distance
            return true
        }
        return false
    }

    fun deleteBestRoute() {
        if (bestRoute != null)
            deleteRoute(bestRoute.signalChannel)
    }

    fun deleteRoute(signalChannel: PeerChannel) {
        routes.remove(signalChannel)
        if (bestRoute != null && bestRoute.signalChannel == signalChannel)
            updateBestRoute()
    }

    fun deleteAllRoutes() {
        bestRoute = null
        routes.clear()
    }

    fun hasRoute() = routes.isNotEmpty()

    private fun updateBestRoute() {
        var bestRoute: SignalRoute? = null
        // Choose the route with minimal distance and maximal timestamp.
        for (route in routes) {
            if (bestRoute == null
                || route.score > bestRoute.score
                || (route.score == bestRoute.score && route.timestamp > bestRoute.timestamp))
                bestRoute = route
        }
        this.bestRoute = bestRoute
        peerAddress.distance =
            bestRoute?.distance
            ?: PeerAddressBook.MAX_DISTANCE + 1
    }

}
