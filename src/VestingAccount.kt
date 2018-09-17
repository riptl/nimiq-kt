import java.io.ByteArrayInputStream

class VestingAccount(
    balance: Satoshi = 0,
    val owner: Address = Address.NULL,
    val vestingStart: UInt = 0,
    val vestingStepBlocks: UInt = 0,
    val vestingStepAmount: ULong = balance,
    val vestingTotalAmount: ULong = balance
) : Contract(balance) {

    override val type: Type
        get() = Account.Type.VESTING

    override fun withBalance(balance: Satoshi) =
        VestingAccount(balance, owner, vestingStart, vestingStepBlocks, vestingTotalAmount)

    override fun withOutgoingTransaction(transaction: Transaction, blockHeight: UInt, txCache: TransactionsCache, revert: Boolean): Account {
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

    fun getMinCap(blockHeight: UInt): UInt =
        if (vestingStepBlocks != 0 && vestingStepAmount > 0) {
            val amount = vestingTotalAmount - ((blockHeight - vestingStart) / vestingStepBlocks) * vestingStepAmount
            if (amount > vestingTotalAmount) 0
            else vestingTotalAmount - amount
        } else {
            0
        }

}
