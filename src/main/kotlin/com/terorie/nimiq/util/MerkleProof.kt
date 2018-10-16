package com.terorie.nimiq.util

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.roundToInt

@ExperimentalUnsignedTypes
class MerkleProof(
    val hashes: Array<HashLight>,
    val ops: Array<Operation>
) {

    enum class Operation(val id: Int) {
        CONSUME_PROOF(0),
        CONSUME_INPUT(1),
        HASH(2),
    }

    companion object : Enc<MerkleProof> {
        private fun compress(ops: Array<Operation>): UByteArray {
            val count = ops.size
            val opBitsSize = ceil(count.toFloat() / 4).roundToInt()
            val opBits = UByteArray(opBitsSize)
            for (i in 0 until count) {
                val op = ops[i].ordinal and 0x3
                opBits[i / 4] = opBits[i / 4] or (op shl ((i % 4) * 2)).toUByte()
            }
            return opBits
        }

        override fun serializedSize(o: MerkleProof): Int {
            val opBitsSize = ceil(o.ops.size.toFloat() / 4).roundToInt()
            var x = 4 + opBitsSize
            x += o.hashes.size * HashLight.SIZE
            return x
        }

        override fun deserialize(s: InputStream): MerkleProof {
            val opCount = s.readUShort()
            val opBitsSize = ceil(opCount.toInt().toFloat() / 4).roundToInt()
            val opBits = s.readFull(opBitsSize)

            val ops = Array(opCount.toInt()) { i ->
                val id = opBits[i / 4].toInt() ushr (i % 4 * 2) and 0x3
                Operation.values()[id]
            }

            val hashes = Array(s.readUShort().toInt()) {
                s.read(HashLight())
            }

            return MerkleProof(hashes, ops)
        }

        override fun serialize(s: OutputStream, o: MerkleProof) = with(o) {
            s.writeUShort(ops.size)
            s.write(compress(ops))
            s.writeUShort(hashes.size)
            for (hash in hashes)
                s.write(hash)
        }
    }

    fun computeRoot(leafValues: List<HashLight>): HashLight = TODO()

}
