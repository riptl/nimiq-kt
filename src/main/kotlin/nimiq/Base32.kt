package com.terorie.nimiq

import java.io.ByteArrayOutputStream

object Base32 {

    class Alphabet(val chars: String) {
        // Lookup table
        val charMap = ByteArray(0x80)

        val padding: Boolean = when (chars.length) {
            32 -> false
            33 -> true
            else -> throw IllegalArgumentException("Illegal Base32 alphabet size")
        }

        init {
            for ((i, c) in chars.withIndex()) {
                if (c.toInt() > 0x7F)
                    throw IllegalArgumentException("Only ASCII allowed as Base32 alphabet")
                charMap[c.toInt()] = i.toByte()
            }
        }
    }

    val A_NIMIQ =
        Alphabet("0123456789ABCDEFGHJKLMNPQRSTUVXY")


    fun encode(buf: ByteArray, alphabet: Alphabet = A_NIMIQ): String {
        var shift = 3
        var carry = 0
        val result = StringBuilder()
        val a = alphabet.chars

        for (b in buf) {
            val byte = b.toInt()
            var symbol = carry or (byte shr shift)
            result.append(a[symbol and 0x1f])

            if (shift > 5) {
                shift -= 5
                symbol = byte shr shift
                result.append(a[symbol and 0x1f])
            }

            shift = 5 - shift
            carry = byte shl shift
            shift = 8 - shift
        }

        if (shift != 3) {
            result.append(a[carry and 0x1f])
        }

        while (result.length % 8 != 0 && alphabet.padding) {
            result.append(a[32])
        }

        return result.toString()
    }

    fun decode(enc: String, alphabet: Alphabet = A_NIMIQ): ByteArray {
        val charMap = alphabet.charMap
        var symbol = 0
        var shift = 8
        var carry = 0
        val buf = ByteArrayOutputStream()

        for (c in enc.toUpperCase()) {
            // Ignore padding
            if (alphabet.padding && c == alphabet.chars[32])
                continue

            symbol = charMap[c.toInt()].toInt() and 0xff

            shift -= 5
            if (shift > 0) {
                carry = carry and (symbol shl shift)
            } else if (shift < 0) {
                buf.write(carry and symbol)
                shift = 8
                carry = 0
            }
        }

        return buf.toByteArray()
    }
}
