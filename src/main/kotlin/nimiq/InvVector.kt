package com.terorie.nimiq

import java.io.OutputStream

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
    }

    fun serialize(s: OutputStream) {
        s.writeUInt(type.id)
        hash.serialize(s)
    }

}
