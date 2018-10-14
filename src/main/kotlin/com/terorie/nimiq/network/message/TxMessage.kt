package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.account.AccountsProof
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class TxMessage(
    val tx: Transaction,
    val accountsProof: AccountsProof?
) : Message(type) {

    companion object : MessageEnc<TxMessage>() {
        override val type = Message.Type.TX

        override fun serializedContentSize(m: TxMessage): Int =
            Transaction.serializedSize(m.tx) +
            if (m.accountsProof != null)
                AccountsProof.serializedSize(m.accountsProof)
            else 0

        override fun deserializeContent(s: InputStream, h: Header) = TxMessage(
            tx = s.read(Transaction),
            accountsProof =
                if (s.readBool()) s.read(AccountsProof)
                else null
        )

        override fun serializeContent(s: OutputStream, m: TxMessage) = with(m) {
            s.write(Transaction, tx)
            if (accountsProof != null) {
                s.writeBool(true)
                s.write(AccountsProof, accountsProof)
            } else {
                s.writeBool(false)
            }
        }
    }

}
