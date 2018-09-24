package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.util.Base32
import com.terorie.nimiq.util.Blob
import com.terorie.nimiq.consensus.primitive.HashLight
import java.util.*
import kotlin.math.ceil

class Address : Blob(SIZE) {

    companion object {
        const val CCODE = "NQ"
        const val SIZE = 20

        val NULL = Address()
        val CONTRACT_CERATION = Address()

        fun fromBase64(s: String) = Address().apply {
            val decoded = Base64.getDecoder().decode(s)
            if (decoded.size == 20) {
                System.arraycopy(decoded, 0, buf, 0, 20)
            } else {
                throw IllegalArgumentException("invalid length")
            }
        }

        fun fromHash(h: HashLight) = Address().apply {
            System.arraycopy(h, 0, buf, 0, 20)
        }

        fun fromUserFriendly(_s: String) = Address().apply {
            val s = _s.replace(" ", "")

            if (s.substring(0..2) != CCODE)
                throw IllegalArgumentException("Invalid address: Wrong country code")
            if (s.length != 36)
                throw IllegalArgumentException("Invalid address: Should be 36 chars (ignoring spaces)")
            if (ibanCheck(s.substring(4) + s.take(4)) != 1)
                throw IllegalArgumentException("Invalid address: Checksum invalid")

            copyFrom(Base32.decode(s))
        }

        private fun ibanCheck(s: String): Int {
            val chars = s.toCharArray().map { _c ->
                val c = _c.toUpperCase()
                if (c in '0'..'9') c
                    else c - 0x37
            }
            val numString = chars.toString()

            var tmp = ""
            for (i in 0..ceil(numString.length / 6f).toInt()) {
                val x = (tmp + numString.substring(i*6..6)).toInt()
                tmp = (x % 97).toString()
            }

            return tmp.toInt()
        }
    }

    fun toUserFriendly(withSpaces: Boolean = true): String {
        val base32 = Base32.encode(buf)
        val verify = ibanCheck(base32 + CCODE + "00")
        val check = ("00" + (98 - verify)).takeLast(2)
        var userFriendly = CCODE + check + base32
        if (withSpaces) {
            userFriendly =
                userFriendly.substring( 0.. 4) + " " +
                userFriendly.substring( 4.. 8) + " " +
                userFriendly.substring( 8..12) + " " +
                userFriendly.substring(12..16) + " " +
                userFriendly.substring(16..20)
        }
        return userFriendly
    }

}
