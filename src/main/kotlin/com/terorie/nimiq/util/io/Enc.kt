package com.terorie.nimiq.util.io

import java.io.InputStream
import java.io.OutputStream

interface Enc<T> {
    fun serializedSize(o: T): Int
    fun serialize(s: OutputStream, o: T)
    fun deserialize(s: InputStream): T
}
