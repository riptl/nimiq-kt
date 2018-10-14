package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class GetTransactionReceiptsMessage(
    val address: Address,
    val offset: UInt = 0U
) : Message(type) {
    
    companion object : MessageEnc<GetTransactionReceiptsMessage>() {
        override val type = Message.Type.GET_TRANSACTION_RECEIPTS

        override fun serializedContentSize(m: GetTransactionReceiptsMessage): Int =
            24

        override fun deserializeContent(s: InputStream, h: Header) = GetTransactionReceiptsMessage(
            s.read(Address()),
            s.readUInt()
        )

        override fun serializeContent(s: OutputStream, m: GetTransactionReceiptsMessage) = with(m) {
            s.write(address)
            s.writeUInt(offset)
        }
    }

}
