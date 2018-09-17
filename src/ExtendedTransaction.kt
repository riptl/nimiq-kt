import java.io.InputStream
import java.io.OutputStream

class ExtendedTransaction(
    sender: Address,
    senderType: Account.Type,
    recipient: Address,
    recipientType: Account.Type,
    value: Satoshi,
    fee: Satoshi,
    validityStartHeight: UByte,
    flags: UByte,
    data: ByteArray,
    proof: ByteArray
) : Transaction(
    sender, senderType,
    recipient, recipientType,
    value, fee,
    validityStartHeight, flags,
    data, proof
) {

    companion object {
        fun unserialize(s: InputStream, needType: Boolean = true): ExtendedTransaction {
            if (!needType) {
                val type = s.readUByte()
                assert(type == Transaction.Format.EXTENDED.ordinal)
            }

            val dataSize = s.readUShort()
            val data = s.readFull(dataSize)
            val sender = Address().apply { unserialize(s) }
            val senderType = Account.Type.values()[s.readUByte()]
            val recipient = Address().apply { unserialize(s) }
            val recipientType = Account.Type.values()[s.readUByte()]
            val value = s.readULong()
            val fee = s.readULong()
            val vsh = s.readUByte()
            val flags = s.readUByte()
            val proofSize = s.readUShort()
            val proof = s.readFull(proofSize)
            return ExtendedTransaction(
                sender, senderType, recipient, recipientType,
                value, fee, vsh, flags, data, proof)
        }
    }

    override val format: Format
        get() = Transaction.Format.EXTENDED

    override fun serialize(s: OutputStream) {
        s.writeUByte(Transaction.Format.EXTENDED.ordinal)
        s.writeUShort(data.size)
        s.write(data)
        sender.serialize(s)
        s.writeUByte(senderType.ordinal)
        recipient.serialize(s)
        s.writeUByte(recipientType.ordinal)
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUByte(validityStartHeight)
        s.writeUByte(flags)
        s.writeUShort(proof.size)
        s.write(proof)
    }

    override val serializedSize: Int
        get() {
            var v = 3 // Static fields
            v +=
        }
}