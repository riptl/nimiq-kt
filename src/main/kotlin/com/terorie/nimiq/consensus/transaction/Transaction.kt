package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.GenesisConfig
import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.util.io.*
import org.bouncycastle.util.Arrays
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
abstract class Transaction(
        val sender: Address,
        val senderType: Account.Type,
        val recipient: Address,
        val recipientType: Account.Type,
        val value: Satoshi,
        val fee: Satoshi,
        val validityStartHeight: UInt,
        val flags: UByte,
        val data: ByteArray,
        val proof: ByteArray,
        val networkId: UByte = GenesisConfig.networkID
) : Comparable<Transaction> {

    companion object {
        const val FLAG_NONE: UByte = 0U
        const val FLAG_CONTACT_CREATION: UByte = 0b1U
        const val FLAG_ALL: UByte = 0b1U

        fun unserialize(s: InputStream) = when (s.readUByte().toInt()) {
            Format.BASIC.ordinal ->
                BasicTransaction.unserialize(s, needType = false)
            Format.EXTENDED.ordinal ->
                ExtendedTransaction.unserialize(s, needType = false)
            else -> throw IllegalArgumentException("Invalid transaction")
        }
    }

    enum class Format(val id: UByte) {
        BASIC(0U), EXTENDED(1U)
    }

    abstract val format: Format

    abstract fun serialize(s: OutputStream)
    abstract val serializedSize: Int

    var _valid: Boolean? = null
    fun verify(networkId: UByte = GenesisConfig.networkID): Boolean {
        if (_valid == null)
            _valid = _verify(networkId)
        return _valid!!
    }

    private fun _verify(networkId: UByte): Boolean {
        if (networkId != this.networkId)
            return false
        if (sender == recipient)
            return false
        if (!Account.verifyOutgoingTransaction(senderType, this))
            return false
        if (!Account.verifyIncomingTransaction(recipientType, this))
            return false
        return true
    }

    var _hash: HashLight? = null
    val hash: HashLight
        get() {
            if (_hash == null)
                _hash = HashLight(assemble { serializeContent(it) })
            return _hash!!
        }

    val feePerByte get() = fee / serializedSize.toULong()

    open fun getContactCreationAddress(): Address {
        // TODO Efficiency
        // Copy with serialize/unserialize
        val tx = unserialize(
                ByteArrayInputStream(
                        ByteArrayOutputStream()
                                .apply { serialize(this) }
                                .toByteArray()
                ))
        Address.NULL.copyTo(tx.recipient)
        tx._hash = null
        return Address.fromHash(tx.hash)
    }

    override fun compareTo(other: Transaction) = when {
        fee / serializedSize.toUInt() > other.fee / other.serializedSize.toUInt() -> -1
        fee / serializedSize.toUInt() < other.fee / other.serializedSize.toUInt() -> +1
        serializedSize > other.serializedSize -> -1
        serializedSize < other.serializedSize -> +1
        fee > other.fee -> -1
        fee < other.fee -> +1
        value > other.value -> -1
        value < other.value -> +1
        else -> compareBlockOrder(other)
    }

    fun compareBlockOrder(other: Transaction) = when {
        recipient > other.recipient -> +1
        recipient < other.recipient -> -1
        validityStartHeight < other.validityStartHeight -> -1
        validityStartHeight > other.validityStartHeight -> +1
        fee > other.fee -> -1
        fee < other.fee -> +1
        value > other.value -> -1
        value < other.value -> +1
        sender > other.sender -> +1
        sender < other.sender -> -1
        recipientType < other.recipientType -> -1
        recipientType > other.recipientType -> +1
        senderType < other.senderType -> -1
        senderType > other.senderType -> +1
        flags < other.flags -> -1
        flags > other.flags -> +1
        else -> Arrays.compareUnsigned(data, other.data)
    }

    fun serializeContent(s: OutputStream) {
        // TODO Size check
        s.writeUShort(data.size.toUShort())
        s.write(data)
        sender.serialize(s)
        s.writeUByte(senderType.id)
        recipient.serialize(s)
        s.writeUByte(recipientType.id)
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUInt(validityStartHeight)
        s.writeUByte(networkId)
        s.writeUByte(flags)
    }

}