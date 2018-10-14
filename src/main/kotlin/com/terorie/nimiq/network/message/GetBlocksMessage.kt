package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
class GetBlocksMessage(
    val locators: Array<HashLight>,
    val maxInvSize: UShort,
    val direction: UByte
) : Message(type) {

    companion object : MessageEnc<GetBlocksMessage>() {
        const val FORWARD: UByte = 0x1U
        const val BACKWARD: UByte = 0x2U
        const val LOCATORS_MAX_COUNT = 128

        override val type = Message.Type.GET_ADDR

        override fun serializedContentSize(m: GetBlocksMessage): Int =
            5 + m.locators.size * HashLight.SIZE

        override fun deserializeContent(s: InputStream, h: Header) = GetBlocksMessage (
            locators = Array(s.readUShort().toInt()) {
                s.read(HashLight())
            },
            maxInvSize = s.readUShort(),
            direction = s.readUByte()
        )

        override fun serializeContent(s: OutputStream, m: GetBlocksMessage) = with(m) {
            s.writeUShort(locators.size)
            for (locator in locators)
                s.write(locator)
            s.writeUShort(maxInvSize)
            s.writeUByte(direction)
        }
    }

    init {
        when (direction) {
            FORWARD, BACKWARD -> Unit
            else -> throw IllegalArgumentException("Invalid direction")
        }
    }

}
