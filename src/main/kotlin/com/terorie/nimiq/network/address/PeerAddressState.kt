package com.terorie.nimiq.network.address

class PeerAddressState(val peerAddress: PeerAddress) {

    companion object {
        const val NEW = 1
        const val ESTABLISHED = 2
        const val TRIED = 3
        const val FAILED = 4
        const val BANNED = 5
    }

    var state = NEW
    var lastConnected = -1
    var bannedUntil = -1
    var banBackOff = PeerAddressBook.INITIAL_FAILED_BACKOFF

    var failedAttempts: Int = 0

}
