package com.terorie.nimiq

import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

@ExperimentalUnsignedTypes
abstract class Account : Serializable {

    enum class Type {
        BASIC,
        VESTING,
        HTLC;

        val id: UByte get() = ordinal.toUByte()
        companion object {
            fun byID(id: UByte) = values()[id.toInt()]
        }
    }

    companion object {
        val INITIAL = BasicAccount(0UL)

        fun verifyIncomingTransaction(type: Type, tx: Transaction) = when(type) {
            Type.BASIC -> BasicAccount.verifyIncomingTransaction(tx)
            Type.VESTING -> VestingContract.verifyIncomingTransaction(tx)
            Type.HTLC -> HashTimeLockedContract.verifyIncomingTransaction(tx)
        }

        fun verifyOutgoingTransaction(type: Type, tx: Transaction) = when(type) {
            Type.BASIC -> BasicAccount.verifyOutgoingTransaction(tx)
            Type.VESTING -> VestingContract.verifyOutgoingTransaction(tx)
            Type.HTLC -> HashTimeLockedContract.verifyOutgoingTransaction(tx)
        }

        fun unserialize(s: InputStream) = when(Type.byID(s.readUByte())) {
            Type.BASIC -> BasicAccount.unserialize(s)
            Type.VESTING -> VestingContract.unserialize(s)
            Type.HTLC -> HashTimeLockedContract.unserialize(s)
        }
    }

    abstract val type: Type
    var balance: Satoshi = 0UL

    open fun withIncomingTransaction(transaction: Transaction, blockHeight: UInt, revert: Boolean = false): Account {
        return if (!revert) {
            withBalance(balance + transaction.value)
        } else {
            if (transaction.value > balance)
                throw IllegalArgumentException("Balance error")
            val newBalance = balance - transaction.value
            withBalance(newBalance)
        }
    }

    open fun withOutgoingTransaction(
            transaction: Transaction,
            blockHeight: UInt,
            txCache: TransactionCache,
            revert: Boolean = false
    ): Account =
        if (!revert) {
            val sub = transaction.value + transaction.fee
            // TODO Check for integer overflows
            if (sub > balance)
                throw IllegalArgumentException("Balance error")
            if (blockHeight < transaction.validityStartHeight ||
                    blockHeight >= transaction.validityStartHeight + Policy.TRANSACTION_VALIDITY_WINDOW)
                throw IllegalStateException("Validity error")
            if (txCache.contains(transaction))
                throw IllegalStateException("Double transaction error")

            withBalance(balance - sub)
        } else {
            if (blockHeight < transaction.validityStartHeight ||
                    blockHeight >= transaction.validityStartHeight + Policy.TRANSACTION_VALIDITY_WINDOW)
                throw IllegalStateException("Validity error")

            withBalance(balance + transaction.value + transaction.fee)
        }

    abstract fun withBalance(balance: Satoshi): Account

    fun serialize(s: OutputStream) {
        s.writeUByte(type.id)
        s.writeULong(balance)
    }

    fun isInitial() = this === Account.INITIAL

    fun isToBePruned() = this.balance == 0UL && !this.isInitial()

}
