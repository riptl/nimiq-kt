package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.GenesisConfig
import com.terorie.nimiq.consensus.account.Account
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.consensus.primitive.Satoshi
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.util.io.*
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
    networkId: UByte = GenesisConfig.networkID,
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
    SignatureProof.serializeToByteArray(
        SignatureProof.singleSig(senderPubKey, signature)),
    networkId
) {

    companion object : Enc<BasicTransaction> {
        override fun serializedSize(o: BasicTransaction) = 138

        override fun deserialize(s: InputStream) = BasicTransaction(
            senderPubKey = s.read(PublicKeyNim()),
            recipient = s.read(Address()),
            value = s.readULong(),
            fee = s.readULong(),
            validityStartHeight = s.readUInt(),
            networkId = s.readUByte(),
            signature = s.read(SignatureNim())
        )

        override fun serialize(s: OutputStream, o: BasicTransaction) = with(o) {
            s.writeUByte(Format.BASIC.id)
            senderPubKey.serialize(s)
            recipient.serialize(s)
            s.writeULong(value)
            s.writeULong(fee)
            s.writeUInt(validityStartHeight)
            s.writeUByte(networkId)
            signature.serialize(s)
        }
    }

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