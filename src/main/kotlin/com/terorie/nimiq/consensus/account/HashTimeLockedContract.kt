package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.Hash
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.transaction.SignatureProof
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.consensus.transaction.TransactionCache
import com.terorie.nimiq.util.io.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class HashTimeLockedContract(
        balance: Satoshi,
        val sender: Address,
        val recipient: Address,
        val hashRoot: Hash,
        val hashCount: UByte,
        val timeout: UInt,
        val totalAmount: ULong
) : Contract(balance) {

    companion object : Enc<HashTimeLockedContract> {
        fun create(balance: Satoshi, transaction: Transaction): HashTimeLockedContract {
            val s = ByteArrayInputStream(transaction.data)

            val sender = s.read(Address())
            val recipient = s.read(Address())
            val hashAlgorithm = Hash.Algorithm.byID(s.readUByte())
            val hashRoot = s.read(Hash(hashAlgorithm))
            val hashCount = s.readUByte()
            val timeout = s.readUInt()
            val totalAmount = s.readULong()
            return HashTimeLockedContract(balance, sender, recipient, hashRoot, hashCount, timeout, totalAmount)
        }

        fun verifyIncomingTransaction(transaction: Transaction): Boolean {
            try {
                val s = ByteArrayInputStream(transaction.proof)

                s.skip(20) // Skip sender address
                s.skip(20) // Skip recipient address
                val hashAlgorithm = Hash.Algorithm.byID(s.readUByte())
                if (hashAlgorithm == Hash.Algorithm.INVALID ||
                        hashAlgorithm == Hash.Algorithm.ARGON2D)
                    return false
                s.skip(hashAlgorithm.size.toLong())
                s.skip(1) // Skip hash count
                s.skip(4) // Skip timeout

                return Contract.verifyIncomingTransaction(transaction)
            } catch (_: Exception) {
                return false
            }
        }

        fun verifyOutgoingTransaction(tx: Transaction): Boolean {
            try {
                val s = ByteArrayInputStream(tx.proof)
                val type = ProofType.byID(s.readUByte())
                when (type) {
                    ProofType.REGULAR_TRANSFER -> {
                        val hashAlgorithm = Hash.Algorithm.byID(s.readUByte())
                        val hashDepth = s.readUByte()
                        val hashRoot = s.read(Hash(hashAlgorithm))
                        val preImage = s.read(Hash(hashAlgorithm))

                        // Verify that the preImage hashed hashDepth times matches the _provided_ hashRoot.
                        for (i in 0 until hashDepth.toInt())
                            preImage.compute(preImage.buf)

                        if (hashRoot != preImage)
                            return false

                        // Signature proof of the HTLC recipient
                        if (!s.read(SignatureProof).verify(null, assemble { tx.serializeContent(it) }))
                            return false
                    }
                    ProofType.EARLY_RESOLVE -> {
                        // Signature proof of the HTLC recipient
                        if (!s.read(SignatureProof).verify(null, assemble { tx.serializeContent(it) }))
                            return false

                        // Signature proof of the HTLC creator
                        if (!s.read(SignatureProof).verify(null, assemble { tx.serializeContent(it) }))
                            return false
                    }
                    ProofType.TIMEOUT_RESOLVE -> {
                        // Signature proof of the HTLC creator
                        if (!s.read(SignatureProof).verify(null, assemble { tx.serializeContent(it) }))
                            return false
                    }
                    else ->
                        return false
                }

                // Reject overlong proof
                if (s.available() > 0)
                    return false

                return true
            } catch (_: Exception) {
                return false
            }
        }

        override fun serializedSize(o: HashTimeLockedContract): Int {
            var size = 8 + 20 + 20 + 1 + 1 + 4 + 8
            size += o.hashRoot.size
            return size
        }

        override fun deserialize(s: InputStream) = HashTimeLockedContract(
            balance = s.readULong(),
            sender = s.read(Address()),
            recipient = s.read(Address()),
            hashRoot = s.read(Hash(Hash.Algorithm.byID(s.readUByte()))), // TODO make nicer
            hashCount = s.readUByte(),
            timeout = s.readUInt(),
            totalAmount = s.readULong()
        )

        override fun serialize(s: OutputStream, o: HashTimeLockedContract) = with(o) {
            s.writeULong(balance)
            s.write(sender)
            s.write(recipient)
            s.writeUByte(hashRoot.algorithm.id)
            s.write(hashRoot)
            s.writeUByte(hashCount)
            s.writeUInt(timeout)
            s.writeULong(totalAmount)
        }
    }

    override val type: Type
        get() = Account.Type.HTLC

    override fun withBalance(balance: Satoshi): Account =
            HashTimeLockedContract(balance, sender, recipient, hashRoot, hashCount, timeout, totalAmount)

    override fun withOutgoingTransaction(transaction: Transaction, blockHeight: UInt, txCache: TransactionCache, revert: Boolean): Account {
        val s = ByteArrayInputStream(transaction.proof)
        val type = ProofType.byID(s.readUByte())
        var minCap = 0UL
        when (type) {
            ProofType.REGULAR_TRANSFER -> {
                // Check that the contract has not expired yet
                if (timeout < blockHeight)
                    throw IllegalArgumentException("Contract expired")

                // Check that the provided hashRoot is correct
                val hashAlgorithm = Hash.Algorithm.byID(s.readUByte())
                val hashDepth = s.readUByte()
                val _hashRoot = s.read(Hash(hashAlgorithm))
                if (_hashRoot != hashRoot)
                    throw IllegalArgumentException("Proof error")

                // Ignore the preImage
                s.skip(hashAlgorithm.size.toLong())

                // Verify that the transaction is signed by the authorized recipient
                if (!s.read(SignatureProof).isSignedBy(recipient))
                    throw IllegalArgumentException("Proof error")

                minCap =
                    if (hashDepth > hashCount) 0U
                    else totalAmount
            }
            ProofType.EARLY_RESOLVE -> {
                // Signature proof of the HTLC recipient
                if (!s.read(SignatureProof).isSignedBy(recipient))
                    throw IllegalArgumentException("Proof error")

                // Signature proof of the HTLC creator
                if (!s.read(SignatureProof).isSignedBy(sender))
                    throw IllegalArgumentException("Proof error")
            }
            ProofType.TIMEOUT_RESOLVE -> {
                if (timeout >= blockHeight)
                    throw IllegalArgumentException("Proof error")

                // Signature proof of the HTLC creator
                if (!s.read(SignatureProof).isSignedBy(sender))
                    throw IllegalArgumentException("Proof error")
            }
            else ->
                throw IllegalArgumentException("Proof error")
        }

        if (!revert) {
            val newBalance = balance - transaction.value - transaction.fee
            if (newBalance < minCap)
                throw IllegalArgumentException("Balance error")
        }

        return super.withOutgoingTransaction(transaction, blockHeight, txCache, revert)
    }

    override fun withIncomingTransaction(transaction: Transaction, blockHeight: UInt, revert: Boolean): Account {
        throw IllegalArgumentException("illegal incoming transaction")
    }

    enum class ProofType {
        INVALID,
        REGULAR_TRANSFER,
        EARLY_RESOLVE,
        TIMEOUT_RESOLVE;

        companion object {
            fun byID(id: UByte) = values()[id.toInt()]
        }
    }

}