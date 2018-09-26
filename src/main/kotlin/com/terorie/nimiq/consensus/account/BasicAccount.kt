package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.transaction.SignatureProof
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class BasicAccount(balance: Satoshi) : Account() {

    init {
        this.balance = balance
    }

    companion object : Enc<BasicAccount> {
        fun verifyIncomingTransaction(tx: Transaction): Boolean {
            if (tx.data.size > 64)
                return false
            return true
        }

        fun verifyOutgoingTransaction(tx: Transaction): Boolean =
                SignatureProof.verifyTransaction(tx)

        override fun serializedSize(o: BasicAccount): Int = TODO()

        override fun deserialize(s: InputStream) =
            BasicAccount(balance = s.readULong())

        override fun serialize(s: OutputStream, o: BasicAccount) =
            s.writeULong(o.balance)
    }

    override val type: Type
        get() = Type.BASIC

    override fun withBalance(balance: Satoshi): Account =
            BasicAccount(balance)

    fun withContractCommand(tx: Transaction, blockHeight: UInt, revert: Boolean = false) {
        if (!revert && tx.recipientType !== Type.BASIC &&
                tx.flags and Transaction.FLAG_CONTACT_CREATION != 0.toUByte())
            when (tx.recipientType) {
                Type.VESTING -> VestingContract.create(balance, blockHeight, tx)
                Type.HTLC -> HashTimeLockedContract.create(balance, tx)
                else -> throw IllegalArgumentException("not a known contract type")
            }
        else
            this
    }

}
