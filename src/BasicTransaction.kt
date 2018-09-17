import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class BasicTransaction(
        val senderPubKey: PublicKeyNim,
        recipient: Address,
        value: Satoshi,
        fee: Satoshi,
        validityStartHeight: UByte,
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
        Transaction.FLAG_NONE,
        ByteArray(0),
        ByteArrayOutputStream().apply{
            SignatureProof.singleSig(senderPubKey, signature).
                    serialize(this)
        }.toByteArray()
) {

    companion object {
        fun unserialize(s: InputStream, needType: Boolean = true): BasicTransaction {
            if (!needType) {
                val type = s.readUByte()
                assert(type == Transaction.Format.BASIC.ordinal)
            }

            return BasicTransaction(
                senderPubKey = PublicKeyNim().apply { unserialize(s) },
                recipient = Address().apply { unserialize(s) },
                value = s.readULong(),
                fee = s.readULong(),
                validityStartHeight = s.readUByte(),
                networkId = s.readUByte(),
                signature = SignatureNim().apply { unserialize(s) }
            )
        }
    }

    override fun serialize(s: OutputStream) {
        s.writeUByte(Transaction.Format.BASIC.ordinal)
        senderPubKey.serialize(s)
        recipient.serialize(s)
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUInt(validityStartHeight)
        s.writeUByte(networkId)
        signature.serialize(s)
    }

    override val serializedSize: Int
        get() = 138

    override val format: Format
        get() = Transaction.Format.BASIC

    override fun getContactCreationAddress(): Address {
        val s = ByteArrayOutputStream()
        s.writeUByte(Transaction.Format.BASIC.ordinal)
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