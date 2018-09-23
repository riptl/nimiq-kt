import java.io.*

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

fun InputStream.readUByte(): Int {
    val n = read()
    return if (n > 0) n
        else throw EOFException()
}

fun OutputStream.writeUByte(x: Int) = write(x)

fun InputStream.readUShort(): Int {
    val buf = readFull(2)
    return (buf[0].toInt() shl 8) or
        buf[1].toInt()
}

fun OutputStream.writeUShort(x: Int) {
    write(x)
    write(x shr 8)
}

fun InputStream.readUInt(): UInt {
    val buf = readFull(4)
    return (
        (buf[0].toLong() shl 24) or
        (buf[1].toLong() shl 16) or
        (buf[2].toLong() shl 8) or
         buf[3].toLong()
    ).toUInt()
}

fun OutputStream.writeUInt(x: UInt) {
    write(x)
    write(x shr  8)
    write(x shr 16)
    write(x shr 24)
}

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

fun assemble(f: (ByteArrayOutputStream) -> Unit): ByteArray =
    ByteArrayOutputStream().apply{f(this)}.toByteArray()
