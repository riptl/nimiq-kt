package com.terorie.nimiq.network.message

import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@ExperimentalUnsignedTypes
abstract class Message(val type: Type) {

    enum class Type(val id: ULong) {
        VERSION(0),
        INV(1),
        GET_DATA(2),
        GET_HEADER(3),
        NOT_FOUND(4),
        GET_BLOCKS(5),
        BLOCK(6),
        HEADER(7),
        TX(8),
        MEMPOOL(9),
        REJECT(10),
        SUBSCRIBE(11),

        ADDR(20),
        GET_ADDR(21),
        PING(22),
        PONG(23),

        SIGNAL(30),

        GET_CHAIN_PROOF(40),
        CHAIN_PROOF(41),
        GET_ACCOUNTS_PROOF(42),
        ACCOUNTS_PROOF(43),
        GET_ACCOUNTS_TREE_CHUNK(44),
        ACCOUNTS_TREE_CHUNK(45),
        GET_TRANSACTIONS_PROOF(47),
        TRANSACTIONS_PROOF(48),
        GET_TRANSACTION_RECEIPTS(49),
        TRANSACTION_RECEIPTS(50),
        GET_BLOCK_PROOF(51),
        BLOCK_PROOF(52),

        GET_HEAD(60),
        HEAD(61),

        VERACK(90),
        ;

        companion object {
            val lookup = HashMap<ULong, Type>()
            init {
                for (type in values())
                    lookup[type.id] = type
            }
        }
    }

    companion object {
        private val encoders = HashMap<Type, Enc<Message>>()

        fun register(type: Message.Type, enc: Enc<Message>) {
            encoders[type] = enc
        }
    }

    fun serialize(s: OutputStream) {
        val encoder = encoders[type]
            ?: throw IllegalStateException("no encoder available for $type")
        encoder.serialize(s, this)
    }

}

@ExperimentalUnsignedTypes
interface MessageEnc<T : Message> : Enc<T> {
    companion object {
        const val MAGIC = 0x42042042U
    }

    class Header(
            val type: Message.Type,
            val length: UInt,
            val checksum: UInt
    )

    override fun serializedSize(o: T): Int =
        12 + varUIntSize(o.type.id) +
        serializedContentSize(o)

    fun serializedContentSize(m: T): Int

    override fun serialize(s: OutputStream, o: T) {
        s.writeUInt(MAGIC)
        s.writeVarUInt(o.type.id)
        s.writeUInt(serializedSize(o).toUInt())
        s.writeUInt(0) // Placeholder checksum
        serializeContent(s, o)
    }

    fun serializeContent(s: OutputStream, m: T)

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

    fun deserializeContent(s: InputStream, h: Header): T
}
