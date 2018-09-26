package com.terorie.nimiq.util.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

interface Enc<T> {
    fun serializedSize(o: T): Int
    fun serialize(s: OutputStream, o: T)
    fun deserialize(s: InputStream): T

    fun serializeToByteArray(o: T): ByteArray {
        val s = ByteArrayOutputStream()
        serialize(s, o)
        return s.toByteArray()
    }

    fun deserializeFromByteArray(b: ByteArray): T {
        val s = ByteArrayInputStream(b)
        return deserialize(s)
    }
}
