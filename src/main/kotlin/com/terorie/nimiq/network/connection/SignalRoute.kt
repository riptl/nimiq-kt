package com.terorie.nimiq.network.connection

import com.terorie.nimiq.network.address.PeerAddressBook

@ExperimentalUnsignedTypes
class SignalRoute(
        val signalChannel: PeerChannel,
        val distance: Int,
        val timestamp: UInt
) {

    var failedAttempts = 0

    val score: Int
        get() =
            ((PeerAddressBook.MAX_DISTANCE - distance) / 2) *
            (1 - (failedAttempts / PeerAddressBook.MAX_FAILED_ATTEMPTS_RTC))

}