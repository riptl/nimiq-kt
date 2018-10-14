package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
class GetTransactionsProofMessage(
    val blockHash: HashLight,
    val addresses: Array<Address>
) : Message(type) {

    companion object : MessageEnc<GetTransactionsProofMessage>() {
        const val ADDRESSES_MAX_COUNT = 256

        override val type = Message.Type.GET_TRANSACTIONS_PROOF

        override fun serializedContentSize(m: GetTransactionsProofMessage): Int =
            HashLight.SIZE + 2 + m.addresses.size * Address.SIZE

        override fun deserializeContent(s: InputStream, h: Header) = GetTransactionsProofMessage(
            blockHash = s.read(HashLight()),
            addresses = Array(s.readUShort().toInt()) {
                s.read(Address())
            }
        )

        override fun serializeContent(s: OutputStream, m: GetTransactionsProofMessage) = with(m) {
            s.write(blockHash)
            s.writeUShort(addresses.size)
            for (address in addresses)
                s.write(address)
        }
    }

    init {
        if (addresses.size > ADDRESSES_MAX_COUNT)
            throw IllegalArgumentException("Too many addresses")
    }

}
