package com.terorie.nimiq.network.message

import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@ExperimentalUnsignedTypes
abstract class Message(val type: Type) {

    enum class Type(val id: ULong) {
        VERSION(0U),
        INV(1U),
        GET_DATA(2U),
        GET_HEADER(3U),
        NOT_FOUND(4U),
        GET_BLOCKS(5U),
        BLOCK(6U),
        HEADER(7U),
        TX(8U),
        MEMPOOL(9U),
        REJECT(10U),
        SUBSCRIBE(11U),

        ADDR(20U),
        GET_ADDR(21U),
        PING(22U),
        PONG(23U),

        SIGNAL(30U),

        GET_CHAIN_PROOF(40U),
        CHAIN_PROOF(41U),
        GET_ACCOUNTS_PROOF(42U),
        ACCOUNTS_PROOF(43U),
        GET_ACCOUNTS_TREE_CHUNK(44U),
        ACCOUNTS_TREE_CHUNK(45U),
        GET_TRANSACTIONS_PROOF(47U),
        TRANSACTIONS_PROOF(48U),
        GET_TRANSACTION_RECEIPTS(49U),
        TRANSACTION_RECEIPTS(50U),
        GET_BLOCK_PROOF(51U),
        BLOCK_PROOF(52U),

        GET_HEAD(60U),
        HEAD(61U),

        VERACK(90U),
        ;

        companion object {
            val lookup = HashMap<ULong, Type>()
            init {
                for (type in values())
                    lookup[type.id] = type
            }
        }
    }

    fun serialize(s: OutputStream) {
        @Suppress("UNCHECKED_CAST")
        val encoder = (MessageEnc.encoders[type]
            ?: throw IllegalStateException("no encoder available for $type"))
            as MessageEnc<Message>
        encoder.serialize(s, this)
    }

}

@Suppress("LeakingThis")
@ExperimentalUnsignedTypes
abstract class MessageEnc<T : Message> : Enc<T> {
    companion object {
        const val MAGIC = 0x42042042U

        val encoders = HashMap<Message.Type, MessageEnc<out Message>>()
    }

    init {
        val lType = type
        if (lType != null)
            encoders[lType] = this
    }

    class Header(
            val type: Message.Type,
            val length: UInt,
            val checksum: UInt
    )

    abstract val type: Message.Type?

    override fun serializedSize(o: T): Int =
        12 + varUIntSize(o.type.id) +
        serializedContentSize(o)

    abstract fun serializedContentSize(m: T): Int

    override fun serialize(s: OutputStream, o: T) {
        s.writeUInt(MAGIC)
        s.writeVarUInt(o.type.id)
        s.writeUInt(serializedSize(o).toUInt())
        s.writeUInt(0U) // Placeholder checksum
        serializeContent(s, o)
    }

    abstract fun serializeContent(s: OutputStream, m: T)

    override fun deserialize(s: InputStream): T {
        val magic = s.readUInt()
        val header = Header(
                type = Message.Type.lookup[s.readVarUInt()]
                        ?: throw IllegalArgumentException("no such message"),
                length = s.readUInt(),
                checksum = s.readUInt()
        )

        // Validate magic.
        if (magic != MAGIC)
            throw IllegalArgumentException("not a message")

        // TODO Validate checksum.
        return deserializeContent(s, header)
    }

    abstract fun deserializeContent(s: InputStream, h: Header): T
}

@ExperimentalUnsignedTypes
abstract class EmptyMessageEnc<T : Message> : MessageEnc<T>() {
    override fun serializedContentSize(m: T) = 0
    override fun serializeContent(s: OutputStream, m: T) = Unit
}
