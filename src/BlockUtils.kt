import java.math.BigInteger
import kotlin.math.*

object BlockUtils {

    fun compactToTarget(compact: UInt): BigInteger {
        val exp = 8 * ((compact shr 24) - 3)
        val base = BigInteger.ONE shl exp
        val factor = BigInteger.valueOf(compact and 0xffffff)
        return base * factor
    }

    fun targetToCompact(target: BigInteger): UInt {
        var size = max(ceil(log2(target.toDouble()) / 8).toInt(), 1)
        val firstMask = 1 shl (size - 1) * 8
        val firstByteBig = target / BigInteger.valueOf(firstMask.toLong())
        val firstByte = firstByteBig.toInt()

        if (firstByte >= 0x80)
            size++

        val followMask = 1 shl (size - 3) * 8
        val followBytesBig = target / BigInteger.valueOf(followMask.toLong())
        val followBytes = followBytesBig.toInt()

        return (size shl 24) or (followBytes and 0xffffff)
    }

    fun getTargetHeight(target: BigInteger): Int =
        ceil(log2(target.toDouble())).toInt()

    fun compactToDifficulty(compact: UInt): BigInteger =
        Policy.BLOCK_TARGET_MAX / compactToTarget(compact)

    fun isProofOfWork(hash: HashHard, target: BigInteger): Boolean {
        val hashInt = BigInteger(1, hash.buf)
        return hashInt <= target
    }

}
