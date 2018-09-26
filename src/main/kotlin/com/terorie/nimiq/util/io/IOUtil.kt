package com.terorie.nimiq.util.io

import com.terorie.nimiq.util.Blob
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import kotlin.text.Charsets.US_ASCII

fun InputStream.readFull(n: Int) =
    ByteArray(n).apply { readFull(this) }

fun InputStream.readFull(b: ByteArray) {
    var left = b.size
    var off = 0
    while (left > 0) {
        val read = read(b, off, left)
        off += read
        left -= read
    }
}

fun <T : Blob> InputStream.read(b: T) =
    b.apply { unserialize(this@read) }

fun OutputStream.write(b: Blob) =
    b.serialize(this)

fun <T> InputStream.read(enc: Enc<T>): T =
    enc.deserialize(this)

fun <T> OutputStream.write(enc: Enc<T>, o: T) =
    enc.serialize(this, o)

@ExperimentalUnsignedTypes
fun InputStream.readUByte(): UByte {
    val n = read()
    return if (n > 0) n.toUByte()
        else throw EOFException()
}

@ExperimentalUnsignedTypes
fun OutputStream.writeUByte(x: Int) = writeUByte(x.toUByte())
@ExperimentalUnsignedTypes
fun OutputStream.writeUByte(x: UByte) = write(x.toInt())

@ExperimentalUnsignedTypes
fun InputStream.readUShort(): UShort {
    val buf = readFull(2)
    return ((buf[0].toInt() shl 8) or
        buf[1].toInt()).toUShort()
}

@ExperimentalUnsignedTypes
fun OutputStream.writeUShort(x: Int) = writeUShort(x.toUShort())
@ExperimentalUnsignedTypes
fun OutputStream.writeUShort(_x: UShort) {
    val x = _x.toInt()
    write(x)
    write(x shr 8)
}

@ExperimentalUnsignedTypes
fun InputStream.readUInt(): UInt {
    val buf = readFull(4)
    return (
        (buf[0].toLong() shl 24) or
        (buf[1].toLong() shl 16) or
        (buf[2].toLong() shl 8) or
         buf[3].toLong()
    ).toUInt()
}

@ExperimentalUnsignedTypes
fun OutputStream.writeUInt(x: UInt) {
    write(((x       ) and 0xFFU).toInt())
    write(((x shr  8) and 0xFFU).toInt())
    write(((x shr 16) and 0xFFU).toInt())
    write(((x shr 24) and 0xFFU).toInt())
}

@ExperimentalUnsignedTypes
fun InputStream.readULong(): ULong {
    val buf = readFull(8)
    return (
        (buf[0].toLong() shl 56) or
        (buf[1].toLong() shl 48) or
        (buf[2].toLong() shl 40) or
        (buf[3].toLong() shl 32) or
        (buf[4].toLong() shl 24) or
        (buf[5].toLong() shl 16) or
        (buf[6].toLong() shl 8) or
         buf[7].toLong()
    ).toULong()
}

@ExperimentalUnsignedTypes
fun OutputStream.writeULong(x: ULong) {
    write(x.toInt())
    write((x shr  8).toInt())
    write((x shr 16).toInt())
    write((x shr 24).toInt())
    write((x shr 32).toInt())
    write((x shr 40).toInt())
    write((x shr 48).toInt())
    write((x shr 56).toInt())
}

@ExperimentalUnsignedTypes
fun InputStream.readVarUInt(): ULong {
    val first = readUByte().toInt()
    return when (first) {
        0xFD -> readUShort().toULong()
        0xFE -> readUInt().toULong()
        0xFF -> readULong()
        else -> first.toULong()
    }
}

@ExperimentalUnsignedTypes
fun OutputStream.writeVarUInt(x: ULong) = when {
    x < 0xFD -> writeUByte(x.toUByte())
    x < 0x1_0000 -> {
        writeUByte(0xFD)
        writeUShort(x.toUShort())
    }
    x < 0x1_0000_0000 -> {
        writeUByte(0xFE)
        writeUInt(x.toUInt())
    }
    else -> {
        writeUByte(0xFF)
        writeULong(x.toULong())
    }
}

@ExperimentalUnsignedTypes
fun varUIntSize(x: ULong) = when {
    x < 0xFD -> 1
    x < 0x1_0000 -> 3
    x < 0x1_0000_0000 -> 5
    else -> 9
}

@ExperimentalUnsignedTypes
fun InputStream.readVarString() =
    String(readFull(readUByte().toInt()), US_ASCII)

@ExperimentalUnsignedTypes
fun OutputStream.writeVarString(s: String) {
    val bytes = s.toByteArray(US_ASCII)
    writeUByte(bytes.size.toUByte())
    write(bytes)
}

@ExperimentalUnsignedTypes
fun varStringSize(s: String): Int =
    s.toByteArray(US_ASCII).size

fun assemble(f: (ByteArrayOutputStream) -> Unit): ByteArray =
    ByteArrayOutputStream().apply{f(this)}.toByteArray()
