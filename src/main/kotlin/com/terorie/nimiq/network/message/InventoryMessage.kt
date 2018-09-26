package com.terorie.nimiq.network.message

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.block.BlockHeader
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.transaction.Transaction
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
class InvVector(val type: Type, val hash: HashLight) {

    enum class Type(val id: UInt) {
        ERROR(0U),
        TRANSACTION(1U),
        BLOCK(2U),
    }

    companion object {
        fun fromBlock(block: Block) =
            InvVector(Type.BLOCK, block.header.hash)

        fun fromHeader(header: BlockHeader) =
            InvVector(Type.BLOCK, header.hash)

        fun fromTransaction(tx: Transaction) =
            InvVector(Type.TRANSACTION, tx.hash)

        fun unserialize(s: InputStream) = InvVector(
            type = Type.values()[s.readUInt().toInt()],
            hash = s.read(HashLight())
        )
    }

    fun serialize(s: OutputStream) {
        s.writeUInt(type.id)
        hash.serialize(s)
    }

}

@ExperimentalUnsignedTypes
class InventoryMessage(
    type: Message.Type,
    val vectors: List<InvVector>
) : Message(type) {

    companion object : MessageEnc<InventoryMessage>() {
        const val VECTORS_MAX_COUNT = 1000

        init {
            val e = MessageEnc.encoders
            e[Message.Type.INV] = this
            e[Message.Type.GET_DATA] = this
            e[Message.Type.GET_HEADER] = this
            e[Message.Type.NOT_FOUND] = this
        }

        // Custom type
        override val type: Type? = null

        override fun deserializeContent(s: InputStream, h: MessageEnc.Header): InventoryMessage {
            val count = s.readUShort().toInt()
            val vectors = ArrayList<InvVector>()
            for (i in 0..count)
                vectors.add(InvVector.unserialize(s))
            return InventoryMessage(h.type, vectors)
        }

        override fun serializeContent(s: OutputStream, m: InventoryMessage) = with(m) {
            s.writeUShort(vectors.size)
            for (vector in vectors)
                vector.serialize(s)
        }

        override fun serializedContentSize(m: InventoryMessage): Int = with(m)
            { 2 + vectors.size * (HashLight.SIZE + 4) }
    }

    init {
        if (vectors.size > VECTORS_MAX_COUNT)
            throw IllegalArgumentException("malformed vectors")
    }

}
