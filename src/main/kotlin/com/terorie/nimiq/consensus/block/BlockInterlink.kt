package com.terorie.nimiq.consensus.block

import com.terorie.nimiq.consensus.GenesisConfig
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.MerkleTree
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.or
import kotlin.math.ceil

@ExperimentalUnsignedTypes
class BlockInterlink(
        val hashes: ArrayList<HashLight>,
        val prevHash: HashLight,
        val repeatBits: ByteArray,
        val compressed: ArrayList<HashLight>
) {

    companion object {
        fun compress(hashes: ArrayList<HashLight>, prevHash: HashLight): BlockInterlink {
            val repeatBitsSize = ceil(hashes.size / 8.0).toInt()
            val repeatBits = ByteArray(repeatBitsSize)

            var lastHash = prevHash
            val compressed = ArrayList<HashLight>()
            for ((i, hash) in hashes.withIndex()) {
                if (hash != lastHash) {
                    compressed += hash
                    lastHash = hash
                } else {
                    val maskBit = (1 shl (7 - (i % 8))).toByte()
                    repeatBits[i/8] = repeatBits[i/8] or maskBit
                }
            }

            return BlockInterlink(hashes, prevHash, repeatBits, compressed)
        }

        fun unserialize(s: InputStream, prevHash: HashLight): BlockInterlink {
            val count = s.readUByte()
            val repeatBitsSize = ceil(count.toInt() / 8.0).toInt()
            val repeatBits = s.readFull(repeatBitsSize)

            var hash = prevHash
            val hashes = ArrayList<HashLight>()
            val compressed = ArrayList<HashLight>()
            for (i in 0 until count.toInt()) {
                val maskBit = (1 shl (7 - (i % 8))).toByte()
                val repeated = (repeatBits[i/8] or maskBit) != 0.toByte()
                if (!repeated) {
                    hash = HashLight().apply { unserialize(s) }
                    compressed += hash
                }
                hashes += hash
            }

            return BlockInterlink(hashes, prevHash, repeatBits, compressed)
        }
    }

    fun serialize(s: OutputStream) {
        // TODO Overflow check
        s.writeUByte(hashes.size.toUByte())
        s.write(repeatBits)
        for (hash in compressed)
            hash.serialize(s)
    }

    val serializedSize: Int
        get() = 1 + repeatBits.size +
            HashLight.SIZE * compressed.size

    private var _hash: HashLight? = null
    val hash: HashLight
        get() {
            if (_hash == null) {
                val hashes = ArrayList<HashLight>()
                hashes += HashLight(this.repeatBits)
                hashes += GenesisConfig.genesisHash
                hashes.addAll(compressed)
                _hash = MerkleTree.computeRoot(hashes)
            }
            return _hash!!
        }

}
