package com.terorie.nimiq

import java.io.OutputStream
import java.io.Serializable

abstract class Account : Serializable {

    enum class Type {
        BASIC,
        VESTING,
        HTLC,
    }

    companion object {
        val INITIAL = BasicAccount(0)

        fun verifyIncomingTransaction(type: Type, tx: Transaction) = when(type) {
            Type.BASIC -> BasicAccount.verifyIncomingTransaction(tx)
            Type.VESTING -> VestingAccount.verifyIncomingTransaction(tx)
            Type.HTLC -> HashTimeLockedContract.verifyIncomingTransaction(tx)
        }

        fun verifyOutgoingTransaction(type: Type, tx: Transaction) = when(type) {
            Type.BASIC -> BasicAccount.verifyOutgoingTransaction(tx)
            Type.VESTING -> VestingAccount.verifyOutgoingTransaction(tx)
            Type.HTLC -> HashTimeLockedContract.verifyOutgoingTransaction(tx)
        }
    }

    abstract val type: Type
    var balance: Satoshi = 0

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

    open fun withOutgoingTransaction(transaction: Transaction, blockHeight: UInt, txCache: TransactionsCache, revert: Boolean = false): Account {
        return if (!revert) {
            val sub = transaction.value + transaction.fee
            // TODO Check for integer overflows
            if (sub > balance)
                throw IllegalArgumentException("Balance error")
            if (blockHeight < transaction.validityStartHeight ||
                    blockHeight >= transaction.validityStartHeight + Policy.TRANSACTION_VALIDITY_WINDOW)
                throw IllegalStateException("Validity error")
            if (txCache.contains(transaction))
                throw IllegalStateException("Double transaction error")

            return withBalance(balance - sub)
        } else {
            if (blockHeight < transaction.validityStartHeight ||
                    blockHeight >= transaction.validityStartHeight + Policy.TRANSACTION_VALIDITY_WINDOW)
                throw IllegalStateException("Validity error")

            return withBalance(balance + transaction.value + transaction.fee)
        }
    }

    abstract fun withBalance(balance: Satoshi): Account

    fun serialize(s: OutputStream) {
        s.writeUByte(type.ordinal)
        s.writeULong(balance)
    }

    fun isInitial() = this === Account.INITIAL

    fun isToBePruned() = this.balance == 0L && !this.isInitial()

}
