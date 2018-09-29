package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.GenesisConfig
import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class ExtendedTransaction(
    sender: Address,
    senderType: Account.Type,
    recipient: Address,
    recipientType: Account.Type,
    value: Satoshi,
    fee: Satoshi,
    validityStartHeight: UInt,
    flags: UByte,
    data: ByteArray,
    proof: ByteArray = ByteArray(0),
    networkID: UByte = GenesisConfig.networkID
) : Transaction(
    sender, senderType,
    recipient, recipientType,
    value, fee,
    validityStartHeight, flags,
    data, proof,
    networkID
) {

    companion object : Enc<ExtendedTransaction> {
        override fun serializedSize(o: ExtendedTransaction) =
            3 + o.proof.size

        override fun deserialize(s: InputStream): ExtendedTransaction {
            val dataSize = s.readUShort()
            val data = s.readFull(dataSize.toInt())
            val sender = s.read(Address())
            val senderType = Account.Type.byID(s.readUByte())
            val recipient = s.read(Address())
            val recipientType = Account.Type.byID(s.readUByte())
            val value = s.readULong()
            val fee = s.readULong()
            val vsh = s.readUInt()
            val networkID = s.readUByte()
            val flags = s.readUByte()
            val proofSize = s.readUShort()
            val proof = s.readFull(proofSize.toInt())
            return ExtendedTransaction(
                sender, senderType, recipient, recipientType,
                value, fee, vsh, flags, data, proof, networkID)
        }

        override fun serialize(s: OutputStream, o: ExtendedTransaction) = with(o) {
            s.writeUByte(Format.EXTENDED.id)
            s.writeUShort(data.size.toUShort())
            s.write(data)
            sender.serialize(s)
            s.writeUByte(senderType.id)
            recipient.serialize(s)
            s.writeUByte(recipientType.id)
            s.writeULong(value)
            s.writeULong(fee)
            s.writeUInt(validityStartHeight)
            s.writeUByte(flags)
            s.writeUShort(proof.size.toUShort())
            s.write(proof)
        }
    }

    override val format: Format
        get() = Format.EXTENDED

}