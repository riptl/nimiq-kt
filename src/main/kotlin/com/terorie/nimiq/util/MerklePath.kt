package com.terorie.nimiq.util

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import org.bouncycastle.util.Arrays
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.ceil

@ExperimentalUnsignedTypes
class MerklePath(val nodes: List<Node> = ArrayList()) {

    companion object {
        fun compute(values: List<HashLight>, leafHash: HashLight): MerklePath {
            val path = ArrayList<Node>()
            doCompute(values, leafHash, path)
            return MerklePath(path)
        }

        private fun doCompute(values: List<HashLight>, leafHash: HashLight, path: ArrayList<Node>): Pair<HashLight, Boolean> {
            if (values.isEmpty()) {
                val hash = HashLight(ByteArray(0))
                return Pair(hash, false)
            }

            if (values.isEmpty()) {
                val hash = values[0]
                return Pair(hash, hash == leafHash)
            }

            val mid = values.size / 2
            val left = values.slice(0..mid)
            val right = values.slice(mid..values.size)
            val leftPart = doCompute(left, leafHash, path)
            val rightPart = doCompute(right, leafHash, path)
            val leftHash = leftPart.first;  val rightHash = rightPart.first
            val leftLeaf = leftPart.second; val rightLeaf = rightPart.second
            val hash = HashLight(Arrays.concatenate(leftHash.buf, rightHash.buf))

            if (leftLeaf) {
                path.add(Node(rightHash, false))
                return Pair(hash, true)
            } else if (rightLeaf) {
                path.add(Node(leftHash, true))
                return Pair(hash, true)
            }

            return Pair(hash, false)
        }

        fun unserialize(s: InputStream): MerklePath {
            val count = s.readUByte().toInt()
            val leftBitsSize = ceil(count / 8.0).toInt()
            val leftBits = s.readFull(leftBitsSize)

            val nodes = Array(count) { i ->
                val cell = leftBits[i / 8].toUByte().toInt()
                val left = (cell and (0x80 shr (i % 8))) != 0
                val hash = HashLight().apply { unserialize(s) }
                Node(hash, left)
            }
            return MerklePath(nodes.toList())
        }
    }

    fun computeRoot(leafHash: HashLight): HashLight {
        var root = leafHash
        for (node in nodes) {
            val left = node.left
            val hash = node.hash
            val concat = ByteArray(2 * HashLight.SIZE)
            if (left) {
                System.arraycopy(hash, 0, concat, 0, HashLight.SIZE)
                System.arraycopy(leafHash, 0, concat, HashLight.SIZE, HashLight.SIZE)
            } else {
                System.arraycopy(leafHash, 0, concat, 0, HashLight.SIZE)
                System.arraycopy(hash, 0, concat, HashLight.SIZE, HashLight.SIZE)
            }
            root = HashLight(concat)
        }
        return root
    }

    fun serialize(s: OutputStream) {
        s.write(nodes.size)

        // Compress nodes
        val leftBitsSize = ceil(nodes.size / 8f).toInt()
        val leftBits = ByteArray(leftBitsSize)

        for ((i, n) in nodes.withIndex()) {
            if (n.left) {
                var x = leftBits[i/8].toInt()
                x = x or (0x80 ushr i % 8)
                leftBits[i/8] = x.toByte()
            }
        }
        s.write(leftBits)
    }

    class Node(val hash: HashLight, val left: Boolean)

}