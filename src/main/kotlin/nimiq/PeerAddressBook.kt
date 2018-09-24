package com.terorie.nimiq

class PeerAddressBook(val networkConfig: NetworkConfig) {

    companion object {
        const val MAX_AGE_WEBSOCKET = 1000 * 60 * 30 // 30 minutes
        const val MAX_AGE_WEBRTC = 1000 * 60 * 15 // 10 minutes
        const val MAX_AGE_DUMB = 1000 * 60 // 1 minute
        const val MAX_DISTANCE = 4
        const val MAX_FAILED_ATTEMPTS_WS = 3
        const val MAX_FAILED_ATTEMPTS_RTC = 2
        const val MAX_TIMESTAMP_DRIFT = 1000 * 60 * 10 // 10 minutes
        const val HOUSEKEEPING_INTERVAL = 1000 * 60 // 1 minute
        const val DEFAULT_BAN_TIME = 1000 * 60 * 10 // 10 minutes
        const val INITIAL_FAILED_BACKOFF = 1000 * 30 // 30 seconds
        const val MAX_FAILED_BACKOFF = 1000 * 60 * 10 // 10 minutes
        const val MAX_SIZE_WS = 10000
        const val MAX_SIZE_WSS = 10000
        const val MAX_SIZE_RTC = 10000
        const val MAX_SIZE = 20500 // Includes dumb peers
        const val MAX_SIZE_PER_IP = 250
        const val SEEDING_TIMEOUT = 1000 * 3 // 3 seconds
    }

    private val store = HashSet<PeerAddressState>()

}
