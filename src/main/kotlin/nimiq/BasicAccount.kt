package com.terorie.nimiq

class BasicAccount(balance: Satoshi) : Account() {

    init {
        this.balance = balance
    }

    companion object {
        fun verifyIncomingTransaction(tx: Transaction): Boolean {
            if (tx.data.size > 64)
                return false
            return true
        }

        fun verifyOutgoingTransaction(tx: Transaction): Boolean =
            SignatureProof.verifyTransaction(tx)
    }

    override val type: Account.Type
        get() = Account.Type.BASIC

    override fun withBalance(balance: Satoshi): Account =
        BasicAccount(balance)

    fun withContractCommand(tx: Transaction, blockHeight: UInt, revert: Boolean = false) {
        if (!revert && tx.recipientType !== Type.BASIC &&
                tx.flags and Transaction.FLAG_CONTACT_CREATION != 0)
            return Account.create(balance, blockHeight, tx)
    }

}
