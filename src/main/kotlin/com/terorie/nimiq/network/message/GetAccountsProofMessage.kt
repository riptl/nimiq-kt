package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

// TODO Merge with GetTransactionsProofMessage

@ExperimentalUnsignedTypes
class GetAccountsProofMessage(
    val blockHash: HashLight,
    val addresses: Array<Address>
) : Message(type) {

    companion object : MessageEnc<GetAccountsProofMessage>() {
        const val ADDRESSES_MAX_COUNT = 256

        override val type = Message.Type.GET_ACCOUNTS_PROOF

        override fun serializedContentSize(m: GetAccountsProofMessage): Int =
            HashLight.SIZE + 2 + m.addresses.size * Address.SIZE

        override fun deserializeContent(s: InputStream, h: Header) = GetAccountsProofMessage(
            blockHash = s.read(HashLight()),
            addresses = Array(s.readUShort().toInt()) {
                s.read(Address())
            }
        )

        override fun serializeContent(s: OutputStream, m: GetAccountsProofMessage) = with(m) {
            s.write(blockHash)
            s.writeUShort(addresses.size)
            for (address in addresses)
                s.write(address)
        }
    }

    init {
        if (addresses.size > GetTransactionsProofMessage.ADDRESSES_MAX_COUNT)
            throw IllegalArgumentException("Too many addresses")
    }

}
