package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.util.readUByte
import com.terorie.nimiq.util.readUInt
import com.terorie.nimiq.util.readULong
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class BasicTransaction(
        val senderPubKey: PublicKeyNim,
        recipient: Address,
        value: Satoshi,
        fee: Satoshi,
        validityStartHeight: UInt,
        networkId: UByte,
        val signature: SignatureNim
) : Transaction(
    senderPubKey.toAddress(),
    Account.Type.BASIC,
    recipient,
    Account.Type.BASIC,
    value,
    fee,
    validityStartHeight,
        FLAG_NONE,
    ByteArray(0),
    ByteArrayOutputStream().apply{
        SignatureProof.singleSig(senderPubKey, signature).
                serialize(this)
    }.toByteArray(),
    networkId
) {

    companion object {
        fun unserialize(s: InputStream, needType: Boolean = true): BasicTransaction {
            if (!needType) {
                val type = s.readUByte()
                assert(type == Format.BASIC.id)
            }

            return BasicTransaction(
                    senderPubKey = PublicKeyNim().apply { unserialize(s) },
                    recipient = Address().apply { unserialize(s) },
                    value = s.readULong(),
                    fee = s.readULong(),
                    validityStartHeight = s.readUInt(),
                    networkId = s.readUByte(),
                    signature = SignatureNim().apply { unserialize(s) }
            )
        }
    }

    override fun serialize(s: OutputStream) {
        s.writeUByte(Format.BASIC.id)
        senderPubKey.serialize(s)
        recipient.serialize(s)
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUInt(validityStartHeight)
        s.writeUByte(networkId)
        signature.serialize(s)
    }

    override val serializedSize get() = 138

    override val format: Format
        get() = Format.BASIC

    override fun getContactCreationAddress(): Address {
        val s = ByteArrayOutputStream()
        s.writeUByte(Format.BASIC.id)
        senderPubKey.serialize(s)
        Address.NULL.serialize(s) // NULL recipient
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUInt(validityStartHeight)
        s.writeUByte(networkId)
        signature.serialize(s)

        return Address.fromHash(HashLight(s.toByteArray()))
    }

}