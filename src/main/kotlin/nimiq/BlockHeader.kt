package com.terorie.nimiq

import com.terorie.nimiq.BlockUtils.compactToDifficulty
import com.terorie.nimiq.BlockUtils.compactToTarget
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

    companion object {
        const val VERSION: UShort = 1U
        val SUPPORTED_VERSIONS = arrayOf(1)
        const val SERIALIZED_SIZE = 146

        fun unserialize(s: InputStream) = BlockHeader (
                prevHash = HashLight().apply { unserialize(s) },
                interlinkHash = HashLight().apply { unserialize(s) },
                bodyHash = HashLight().apply { unserialize(s) },
                accountsHash = HashLight().apply { unserialize(s) },
                nBits = s.readUInt(),
                height = s.readUInt(),
                timestamp = s.readUInt(),
                nonce = s.readUInt(),
                version = s.readUShort()
        )
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
        get() = compactToTarget(nBits)

    val difficulty: BigInteger
        get() = compactToDifficulty(nBits)

    var _hash: HashLight? = null
    val hash: HashLight
        get() {
            if (_hash == null)
                _hash = HashLight(assemble{serialize(it)})
            return _hash!!
        }

    var _pow: HashHard? = null
    val pow: HashHard
        get() {
            if (_pow == null)
                _pow = HashHard(assemble{serialize(it)})
            return _pow!!
        }

    fun invalidateCache() {
        _hash = null
        _pow = null
    }

    fun serialize(s: OutputStream) {
        s.writeUShort(version)
        prevHash.serialize(s)
        interlinkHash.serialize(s)
        bodyHash.serialize(s)
        accountsHash.serialize(s)
        s.writeUInt(nBits)
        s.writeUInt(height)
        s.writeUInt(timestamp)
        s.writeUInt(nonce)
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

        // Check that the hahs of the predecessor block equals prevHash.
        if (prevHash != prevHeader.hash)
            return false

        // Everything checks out.
        return true
    }

}
