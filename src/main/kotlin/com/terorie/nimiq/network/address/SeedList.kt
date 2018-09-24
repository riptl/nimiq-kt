package com.terorie.nimiq.network.address

import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import java.net.URL

class SeedList {
    companion object {
        const val MAX_SIZE = 1024 * 128 // 128 kB
        const val REQUEST_TIMEOUT = 8000 // 8 seconds
    }
}

class SeedListURL(val url: URL, val publicKey: PublicKeyNim)
