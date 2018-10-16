package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.account.AccountsProof
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class AccountsProofMessage(
    val blockHash: HashLight,
    val accountsProof: AccountsProof? = null
) : Message(type) {

    companion object : MessageEnc<AccountsProofMessage>() {
        override val type = Message.Type.ACCOUNTS_PROOF

        override fun serializedContentSize(m: AccountsProofMessage): Int {
            var x = 1 + HashLight.SIZE
            if (m.accountsProof != null)
                x += AccountsProof.serializedSize(m.accountsProof)
            return x
        }

        override fun deserializeContent(s: InputStream, h: Header): AccountsProofMessage {
            val blockHash = s.read(HashLight())
            val hasProof = s.readBool()
            return if (hasProof)
                AccountsProofMessage(blockHash, s.read(AccountsProof))
            else
                AccountsProofMessage(blockHash)
        }

        override fun serializeContent(s: OutputStream, m: AccountsProofMessage) = with(m) {
            s.write(blockHash)
            val hasProof = accountsProof != null
            s.writeBool(hasProof)
            if (hasProof)
                s.write(AccountsProof, accountsProof!!)
        }
    }

}
