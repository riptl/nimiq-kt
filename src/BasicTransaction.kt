import java.io.ByteArrayOutputStream
import java.io.InputStream

class BasicTransaction(
        val senderPubKey: PublicKeyNim,
        recipient: Address,
        value: Satoshi,
        fee: Satoshi,
        validityStartHeight: UByte,
        val networkId: UByte,
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

    override val format: Format
        get() = Transaction.Format.BASIC

}