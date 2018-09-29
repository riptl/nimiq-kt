package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.DummyData
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.consensus.transaction.BasicTransaction
import com.terorie.nimiq.consensus.transaction.ExtendedTransaction
import com.terorie.nimiq.consensus.transaction.Transaction
import org.junit.Test
import kotlin.test.*

@ExperimentalUnsignedTypes
class BasicAccountTest {

    private val pubKey = DummyData.pubKey1
    private val recipient = DummyData.address1

    @Test fun enc() {
        val srcAccount = BasicAccount(100U)
        val bytes = Account.serializeToByteArray(srcAccount)
        val gotAccount = Account.deserializeFromByteArray(bytes)

        gotAccount as BasicAccount
        assertEquals(srcAccount.type, gotAccount.type)
        assertEquals(srcAccount.balance, gotAccount.balance)
    }

    @Test fun balanceChange() {
        val account0 = BasicAccount(0U)
        val account10 = account0.withBalance(10U)

        assertEquals(0U, account0.balance)
        assertEquals(10U, account10.balance)
    }

    @Test fun acceptIncomingTx() {
        var tx: Transaction

        tx = BasicTransaction(
            senderPubKey = pubKey,
            recipient = recipient,
            value = 100U,
            fee = 0U,
            validityStartHeight = 0U,
            signature = SignatureNim()
        )
        assertTrue(BasicAccount.verifyIncomingTransaction(tx))

        tx = ExtendedTransaction(
            sender = recipient,
            senderType = Account.Type.BASIC,
            recipient = recipient,
            recipientType = Account.Type.BASIC,
            value = 100U,
            fee = 0U,
            validityStartHeight = 0U,
            flags = Transaction.FLAG_NONE,
            data = ByteArray(60)
        )
        assertTrue(BasicAccount.verifyIncomingTransaction(tx))
    }

    @Test fun denyIncomingTx() {
        fun genTx(data: ByteArray) =
            ExtendedTransaction(
                sender = recipient,
                senderType = Account.Type.BASIC,
                recipient = recipient,
                recipientType = Account.Type.BASIC,
                value = 100U,
                fee = 0U,
                validityStartHeight = 0U,
                flags = Transaction.FLAG_NONE,
                data = data
            )

        var tx: Transaction

        tx = genTx(ByteArray(65))
        assertFalse(BasicAccount.verifyIncomingTransaction(tx))

        tx = genTx(ByteArray(1000))
        assertFalse(BasicAccount.verifyIncomingTransaction(tx))
    }

    @Test fun applyIncomingTx() {
        var account: Account = BasicAccount(0U)
        assertEquals(0U, account.balance)

        var tx: Transaction

        tx = genTx(100U)
        account = account.withIncomingTransaction(tx, 1U)
        assertEquals(100U, account.balance)

        tx = genTx(1U)
        account = account.withIncomingTransaction(tx, 2U)
        assertEquals(101U, account.balance)
    }

    @Test fun revertIncomingTx() {
        var account: Account = BasicAccount(0)
        val tx = genTx(100)

        assertEquals(0U, account.balance)

        account = account.withIncomingTransaction(tx, 1U)
        assertEquals(100U, account.balance)

        account = account.withIncomingTransaction(tx, 1U, revert = true)
        assertEquals(0U, account.balance)
    }

    // TODO Test verify invalid outgoing tx
    // TODO Test verify valid outgoing tx
    // TODO Test apply outgoing tx
    // TODO Test don't apply invalid outgoing tx
    // TODO Test revert outgoing tx

    private fun genTx(value: Satoshi) =
        BasicTransaction(pubKey, recipient, 100U, 0U, 0U, signature = SignatureNim())

}
