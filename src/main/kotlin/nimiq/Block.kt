package com.terorie.nimiq

import com.terorie.nimiq.BlockUtils.getHashDepth
import com.terorie.nimiq.BlockUtils.getTargetDepth
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.util.*
import kotlin.math.max

@ExperimentalUnsignedTypes
class Block(
    val header: BlockHeader,
    val interlink: BlockInterlink,
    val body: BlockBody?
) {

    companion object {
        fun unserialize(s: InputStream): Block {
            val header = BlockHeader.unserialize(s)
            val interlink = BlockInterlink.unserialize(s, header.prevHash)

            var body: BlockBody? = null
            val bodyPresent = s.readUByte().toInt()
            if (bodyPresent != 0)
                body = BlockBody.unserialize(s)

            return Block(header, interlink, body)
        }
    }

    fun serialize(s: OutputStream) {
        header.serialize(s)
        interlink.serialize(s)

        if (body != null) {
            s.writeUByte(1)
            body.serialize(s)
        } else {
            s.writeUByte(0)
        }
    }

    inline val isLight: Boolean
        get() = body == null

    inline val isFull: Boolean
        get() = body != null

    val serializedSize: Int
        get() {
            var v = 147 // header + bodyPresent bool
            v += interlink.serializedSize
            v += body?.serializedSize ?: 0
            return v
        }

    private var verified = false

    fun verify(time: Date): Boolean {
        // Check that the timestamp is not too far into the future.
        if (header.timestamp * 1000U > TODO()) {
            return false
        }

        // Check that the header hash matches the difficulty.
        if (!header.verifyProofOfWork()) {
            return false
        }

        // Check that the maximum block size is not exceeded.
        if (serializedSize > Policy.BLOCK_SIZE_MAX) {
            return false
        }

        // Verify that the interlink is valid.
        if (!verifyInterlink()) {
            return false
        }

        // Verify the body if it is present
        if (isFull && !verifyBody()) {
            return false
        }

        throw TODO()
    }

    private fun verifyInterlink(): Boolean {
        // TODO Skip check for genesis block due to the cyclic dependency (since the interlink hash contains the genesis block hash).

        // Check that the interlinkHash given in the header matches the actual interlinkHash.
        if (header.interlinkHash != interlink.hash) {
            return false
        }

        // Everything checks out.
        return true
    }

    private fun verifyBody(): Boolean {
        // Check that the body is valid
        if (!body!!.verify()) {
            return false
        }

        // Check that bodyHash given in the header matches the actual body hash.
        if (header.bodyHash != body.hash) {
            return false
        }

        // Everything checks out.
        return true
    }

    fun isImmediateSuccessorOf(predecessor: Block): Boolean {
        // Check the header.
        if (!header.isImmediateSuccessorOf(predecessor.header))
            return false

        // Check that the interlink is correct.
        val interlink = predecessor.getNextInterlink(header.target, header.version)
        if (this.interlink != interlink)
            return false

        // Everything checks out.
        return true
    }

    fun isInterlinkSuccessorOf(predecessor: Block): Boolean {
        if (header.height <= predecessor.header.height) {
            return false
        }

        if (header.timestamp <= predecessor.header.timestamp) {
            return false
        }

        // Check that the predecessor is contained in this block's interlink and verify its position.
        val prevHash = predecessor.header.hash
        if (GenesisConfig.genesisHash != prevHash) {
            val prevPow = predecessor.header.pow
            val targetHeight = BlockUtils.getTargetHeight(header.target)
            var blockFound = false

            for (depth in 0 until interlink.hashes.size) {
                if (prevHash == interlink.hashes[depth]) {
                    blockFound = true
                    if (!BlockUtils.isProofOfWork(prevPow, BlockUtils.powerOfTwo(targetHeight - depth))) {
                        return false
                    }
                }
            }

            if (!blockFound) {
                return false
            }
        }

        // If the predecessor happens to be the immediate predecessor, check additionally:
        // - that the height of the successor is one higher
        // - that the interlink is correct.
        if (header.prevHash == prevHash) {
            if (header.height != predecessor.header.height + 1U) {
                return false
            }

            val interlink = predecessor.getNextInterlink(header.target, header.version)
            if (header.interlinkHash == interlink.hash) {
                return false
            }
        }
        // Otherwise, if the prevHash doesn't match but the blocks should be adjacent according to their height fields,
        // this cannot be a valid successor of predecessor.
        else if (header.height == predecessor.header.height + 1U) {
            return false
        }
        // Otherwise, check that the interlink construction is valid given the information we have.
        else {
            // TODO Take different targets into account.

            // The number of new blocks in the interlink is bounded by the height difference.
            val hashes = interlink.hashes.toHashSet()
            hashes.removeAll(predecessor.interlink.hashes)
            if (hashes.size.toUInt() > header.height - predecessor.header.height) {
                return false
            }

            // Check that the interlink is not too short.
            val thisDepth = BlockUtils.getTargetDepth(header.target)
            val prevDepth = BlockUtils.getTargetDepth(predecessor.header.target)
            val depthDiff = thisDepth - prevDepth
            if (interlink.hashes.size < predecessor.interlink.hashes.size - depthDiff) {
                return false
            }

            // If the same block is found in both interlinks, all blocks at lower depths must be the same in both interlinks.
            var commonBlock = false
            val thisInterlink = interlink.hashes
            val prevInterlink = predecessor.interlink.hashes
            var i = 1
            while (i < prevInterlink.size && i -depthDiff < thisInterlink.size) {
                if (prevInterlink[i] == thisInterlink[i - depthDiff]) {
                    commonBlock = true
                } else if (commonBlock) {
                    return false
                }
                i++
            }
        }

        // Everything checks out.
        return true
    }

    fun isSuccessorOf(predecessor: Block) =
        isImmediateSuccessorOf(predecessor) ||
        isInterlinkSuccessorOf(predecessor)

    fun getNextInterlink(nextTarget: BigInteger, nextVersion: UShort = BlockHeader.VERSION): BlockInterlink {
        val hashes = ArrayList<HashLight>()

        // Compute how many times this blockHash should be included in the next interlink.
        val thisPowDepth = getHashDepth(header.pow.toGeneric())
        val nextTargetDepth = getTargetDepth(nextTarget)
        val numOccurrences = max(thisPowDepth - nextTargetDepth + 1, 0)

        // Push this blockHash numOccurrences times onto the next interlink.
        for (i in 0 until numOccurrences)
            hashes += header.hash

        // Compute how many blocks to omit from the beginning of this interlink.
        val thisTargetDepth = getTargetDepth(header.target)
        val targetOffset = nextTargetDepth - thisTargetDepth
        val interlinkOffset = numOccurrences + targetOffset

        // Push the remaining hashes from this interlink.
        for (i in interlinkOffset until interlink.hashes.size)
            hashes += interlink.hashes[i]

        return BlockInterlink.compress(hashes, header.hash)
    }

}