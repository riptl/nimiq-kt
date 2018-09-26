package com.terorie.nimiq.consensus.block

import com.terorie.nimiq.consensus.primitive.HashHard
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger

@ExperimentalUnsignedTypes
class BlockHeader(
        val prevHash: HashLight,
        val interlinkHash: HashLight,
        val bodyHash: HashLight,
        val accountsHash: HashLight,
        val nBits: UInt,
        val height: UInt,
        val timestamp: UInt,
        nonce: UInt,
        val version: UShort
) {

    companion object : Enc<BlockHeader> {
        const val VERSION: UShort = 1U
        val SUPPORTED_VERSIONS = arrayOf(1)

        override fun serializedSize(o: BlockHeader) = 146

        override fun deserialize(s: InputStream) = BlockHeader(
            prevHash =      s.read(HashLight()),
            interlinkHash = s.read(HashLight()),
            bodyHash =      s.read(HashLight()),
            accountsHash =  s.read(HashLight()),
            nBits =         s.readUInt(),
            height =        s.readUInt(),
            timestamp =     s.readUInt(),
            nonce =         s.readUInt(),
            version =       s.readUShort()
        )

        override fun serialize(s: OutputStream, o: BlockHeader) = with(o) {
            s.writeUShort(version)
            s.write(prevHash)
            s.write(interlinkHash)
            s.write(bodyHash)
            s.write(accountsHash)
            s.writeUInt(nBits)
            s.writeUInt(height)
            s.writeUInt(timestamp)
            s.writeUInt(nonce)
        }
    }

    var nonce: UInt = 0U
        set(value) {
            invalidateCache()
            field = value
        }

    init {
        this.nonce = nonce
        // TODO Check if nBits valid
    }

    val target: BigInteger
        get() = BlockUtils.compactToTarget(nBits)

    val difficulty: BigInteger
        get() = BlockUtils.compactToDifficulty(nBits)

    var _hash: HashLight? = null
    val hash: HashLight
        get() {
            if (_hash == null)
                _hash = HashLight(serializeToByteArray(this))
            return _hash!!
        }

    var _pow: HashHard? = null
    val pow: HashHard
        get() {
            if (_pow == null)
                _pow = HashHard(serializeToByteArray(this))
            return _pow!!
        }

    fun invalidateCache() {
        _hash = null
        _pow = null
    }

    fun verifyProofOfWork(): Boolean =
            BlockUtils.isProofOfWork(pow, target)

    fun isImmediateSuccessorOf(prevHeader: BlockHeader): Boolean {
        // Check that the height is one higher than the previous height.
        if (height != prevHeader.height + 1U)
            return false

        // Check that the timestamp is greater or equal to the predecessor's timestamp.
        if (timestamp < prevHeader.timestamp)
            return false

        // Check that the hash of the predecessor block equals prevHash.
        if (prevHash != prevHeader.hash)
            return false

        // Everything checks out.
        return true
    }

}
