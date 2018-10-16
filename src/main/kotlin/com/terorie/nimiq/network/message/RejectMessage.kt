package com.terorie.nimiq.network.message

import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class RejectMessage(
    val messageType: Message.Type,
    val code: Code,
    val reason: String,
    val extraData: ByteArray = ByteArray(0)
) : Message(type) {

    enum class Code(val code: UByte) {
        REJECT_MALFORMED(0x01U),
        REJECT_INVALID(0x10U),
        REJECT_OBSOLETE(0x11U),
        REJECT_DOUBLE(0x12U),
        REJECT_DUST(0x41U),
        REJECT_INSUFFICIENT_FEE(0x42U);

        companion object {
            val lookup = HashMap<UByte, Code>().apply {
                for (v in values())
                    put(v.code, v)
            }
        }
    }

    @ExperimentalUnsignedTypes
    companion object : MessageEnc<RejectMessage>() {
        override val type = Message.Type.REJECT

        override fun serializedContentSize(m: RejectMessage): Int {
            TODO("not implemented: serializedContentSize")
        }

        override fun deserializeContent(s: InputStream, h: Header) = RejectMessage(
            messageType = Message.Type.lookup[s.readVarUInt()]!!,
            code = Code.lookup[s.readUByte()]!!,
            reason = s.readVarString(),
            extraData = s.readFull(s.readUShort().toInt())
        )

        override fun serializeContent(s: OutputStream, m: RejectMessage) = with(m) {
            s.writeVarUInt(messageType.id)
            s.writeUByte(code.code)
            s.writeVarString(reason)
            s.writeUShort(extraData.size)
            s.write(extraData)
        }
    }

}
