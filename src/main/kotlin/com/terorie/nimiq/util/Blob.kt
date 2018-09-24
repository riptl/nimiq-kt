package com.terorie.nimiq.util

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*

open class Blob: Comparable<Blob> {

    val size: Int
    val buf: ByteArray

    constructor(size: Int) {
        this.size = size
        this.buf = ByteArray(size)
    }

    constructor(buf: ByteArray) {
        this.size = buf.size
        this.buf = buf
    }

    val hash get() = HashLight(buf)

    fun copyFrom(src: ByteArray) =
        if (src.size == size)
            System.arraycopy(src, 0, buf, 0, size)
        else throw IllegalArgumentException("size mismatch")

    fun copyTo(dst: Blob) =
        System.arraycopy(buf, 0, dst, 0, size)

    fun serialize(s: OutputStream) = s.write(buf)

    fun unserialize(s: InputStream) = s.readFull(buf)

    override fun equals(other: Any?) =
        if (other is Blob)
            Arrays.equals(buf, other.buf)
        else false

    override fun hashCode() = buf.hashCode()

    override fun compareTo(other: Blob): Int {
        val sizeCompare = size.compareTo(other.size)
        if (sizeCompare != 0)
            return sizeCompare

        for (i in 0 until size) {
            val a = buf[i].toInt() and 0xFF
            val b = other.buf[i].toInt() and 0xFF
            val byteCompare = a.compareTo(b)
            if (byteCompare != 0)
                return byteCompare
        }

        return 0
    }

}