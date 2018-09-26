package com.terorie.nimiq.network

class DataChannel {

    companion object {
        const val CHUNK_SIZE_MAX = 1024 * 16 // 16 kiB
        const val MESSAGE_SIZE_MAX = 10 * 1024 * 1024 // 10 MiB
        const val CHUNK_TIMEOUT = 1000 * 5 // 5 seconds
        const val MESSAGE_TIMEOUT = 120

    }

}
