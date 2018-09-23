package com.terorie.nimiq

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@ExperimentalUnsignedTypes
class VestingContract(
    balance: Satoshi = 0UL,
    val owner: Address = Address.NULL,
    val vestingStart: UInt = 0U,
    val vestingStepBlocks: UInt = 0U,
    val vestingStepAmount: ULong = balance,
    val vestingTotalAmount: ULong = balance
) : Contract(balance) {

    companion object {
        fun create(balance: Satoshi, blockHeight: UInt, tx: Transaction): VestingContract {
            val s = ByteArrayInputStream(tx.data)
            val owner = Address().apply { unserialize(s) }
            val vestingTotalAmount: ULong
            val vestingStart: UInt
            val vestingStepBlocks: UInt
            val vestingStepAmount: ULong
            when (tx.data.size) {
                Address.SIZE + 4 -> {
                    // Only bock number: vest full amount at that block
                    vestingStart = 0U
                    vestingStepBlocks = s.readUInt()
                    vestingStepAmount = tx.value
                    vestingTotalAmount = tx.value
                }
                Address.SIZE + 16 -> {
                    vestingStart = s.readUInt()
                    vestingStepBlocks = s.readUInt()
                    vestingStepAmount = s.readULong()
                    vestingTotalAmount = tx.value
                }
                Address.SIZE + 24 -> {
                    // Create a vesting account with some instantly vested funds or additional funds considered.
                    vestingStart = s.readUInt()
                    vestingStepBlocks = s.readUInt()
                    vestingStepAmount = s.readULong()
                    vestingTotalAmount = s.readULong()
                }
                else -> throw java.lang.IllegalArgumentException("invalid transaction data")
            }
            return VestingContract(balance, owner,
                vestingStart, vestingStepBlocks, vestingStepAmount, vestingTotalAmount)
        }

        fun verifyIncomingTransaction(tx: Transaction): Boolean =
            when (tx.data.size) {
                Address.SIZE + 4,
                Address.SIZE + 16,
                Address.SIZE + 24
                    -> Contract.verifyIncomingTransaction(tx)
                else -> false
            }

        fun verifyOutgoingTransaction(tx: Transaction): Boolean {
            val proofStream = ByteArrayInputStream(tx.proof)
            val proof = SignatureProof.unserialize(proofStream)

            val txData = ByteArrayOutputStream()
                    .apply { tx.serialize(this) }
                    .toByteArray()

            if (!proof.verify(sender = null, data = txData))
                return false

            if (proofStream.available() > 0)
                return false

            return true
        }

        fun unserialize(s: InputStream) = VestingContract(
            balance = s.readULong(),
            owner = Address().apply { unserialize(s) },
            vestingStart = s.readUInt(),
            vestingStepBlocks = s.readUInt(),
            vestingStepAmount = s.readULong(),
            vestingTotalAmount = s.readULong()
        )
    }

    override val type: Type
        get() = Account.Type.VESTING

    override fun withBalance(balance: Satoshi) =
        VestingContract(balance, owner, vestingStart, vestingStepBlocks, vestingTotalAmount)

    override fun withOutgoingTransaction(transaction: Transaction, blockHeight: UInt, txCache: TransactionCache, revert: Boolean): Account {
        if (!revert) {
            val minCap = getMinCap(blockHeight)
            val newBalance = balance - transaction.value - transaction.fee
            if (newBalance < minCap)
                throw IllegalArgumentException("Balance error")

            val proofBuf = ByteArrayInputStream(transaction.proof)
            if (!SignatureProof.unserialize(proofBuf).isSignedBy(owner))
                throw IllegalArgumentException("Proof error")
        }
        return super.withOutgoingTransaction(transaction, blockHeight, txCache, revert)
    }

    override fun withIncomingTransaction(transaction: Transaction, blockHeight: UInt, revert: Boolean): Account {
        throw IllegalArgumentException("Illegal incoming transaction")
    }

    fun getMinCap(blockHeight: UInt): ULong =
        if (vestingStepBlocks != 0U && vestingStepAmount > 0) {
            val amount = vestingTotalAmount - ((blockHeight - vestingStart) / vestingStepBlocks) * vestingStepAmount
            if (amount > vestingTotalAmount) 0U
            else vestingTotalAmount - amount
        } else {
            0UL
        }

}
