package com.terorie.nimiq

import java.io.OutputStream

@ExperimentalUnsignedTypes
abstract class Subscription(val type: Type) {

    enum class Type(val id: UByte) {
        NONE(0U),
        ANY(1U),
        ADDRESSES(2U),
        MIN_FEE(3U),
    }

    abstract fun matchesBlock(block: Block): Boolean
    abstract fun matchesTransaction(tx: Transaction): Boolean

    open fun serialize(s: OutputStream) {
        s.writeUByte(type.id)
    }

}

@ExperimentalUnsignedTypes
object SubscriptionNone : Subscription(Type.NONE) {

    override fun matchesBlock(block: Block) = false
    override fun matchesTransaction(tx: Transaction) = false

}

@ExperimentalUnsignedTypes
object SubscriptionAny : Subscription(Type.ANY) {

    override fun matchesBlock(block: Block) = true
    override fun matchesTransaction(tx: Transaction) = true

}

@ExperimentalUnsignedTypes
class SubscriptionAddresses(val addresses: ArrayList<Address>) : Subscription(Type.ADDRESSES) {

    override fun matchesBlock(block: Block) = true

    override fun matchesTransaction(tx: Transaction) =
        addresses.contains(tx.recipient)
        || addresses.contains(tx.sender)

    override fun serialize(s: OutputStream) {
        super.serialize(s)
        s.writeUShort(addresses.size.toUShort())
        for (address in addresses)
            address.serialize(s)
    }

}

@ExperimentalUnsignedTypes
class SubscriptionMinFee(val minFeePerByte: Satoshi) : Subscription(Type.MIN_FEE) {

    override fun matchesBlock(block: Block) = true

    override fun matchesTransaction(tx: Transaction) =
        tx.fee / tx.serializedSize.toULong() >= minFeePerByte

    override fun serialize(s: OutputStream) {
        super.serialize(s)
        s.writeULong(minFeePerByte)
    }

}
