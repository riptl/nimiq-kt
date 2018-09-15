import java.io.InputStream
import java.io.OutputStream

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
    val networkId: UByte = GenesisConfig.NETWORK_ID
) {

    companion object {
        const val FLAG_NONE: UByte = 0
        const val FLAG_CONTACT_CREATION: UByte = 0b1
        const val FLAG_ALL: UByte = 0b1

        const val FORMAT_BASIC: UByte = 0
        const val TX_FORMAT_EXTENDED: UByte = 1

        fun unserialize(s: InputStream) = when (s.readUByte()) {
            Format.BASIC.ordinal ->
                BasicTransaction.unserialize(s, needType = false)
            Format.EXTENDED.ordinal ->
                ExtendedTransaction.unserialize(s, needType = false)
            else -> throw IllegalArgumentException("Invalid transaction")
        }
    }

    enum class Format {
        BASIC, EXTENDED
    }

    abstract val format: Format
    var lastHash: HashLight? = null

    abstract fun serialize(s: OutputStream)

    var _valid: Boolean? = null
    fun verify(networkId: Int): Boolean {
        if (_valid == null)
            _valid = _verify(networkId)
        return _valid!!
    }

    private fun _verify(networkId: Int): Boolean {
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

    fun hash(): Hash {
        if (lastHash != null)
            lastHash = HashLight()
    }

    fun getContactCreationAddress(): Address {

    }

    fun serializeContent(s: OutputStream) {
        s.writeUShort(data.size)
        s.write(data)
        sender.serialize(s)
        s.writeUByte(senderType.ordinal)
        recipient.serialize(s)
        s.writeUByte(recipientType.ordinal)
        s.writeULong(value)
        s.writeULong(fee)
        s.writeUInt(validityStartHeight)
        s.writeUByte(networkId)
        s.writeUByte(flags)
    }

}