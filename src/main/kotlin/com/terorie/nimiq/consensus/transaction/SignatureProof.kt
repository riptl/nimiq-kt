package com.terorie.nimiq.consensus.transaction

import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.consensus.primitive.SignatureNim
import com.terorie.nimiq.util.MerklePath
import com.terorie.nimiq.util.io.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
class SignatureProof(
        val publicKey: PublicKeyNim,
        val merklePath: MerklePath,
        val signature: SignatureNim?
) {

    companion object : Enc<SignatureProof> {
        fun verifyTransaction(t: Transaction): Boolean {
            val buffer = t.proof
            val inStream = ByteArrayInputStream(buffer)
            val proof = inStream.read(SignatureProof)

            // Reject proof if it is longer than needed.
            if (inStream.available() > 0)
                return false

            return proof.verify(t.sender,
                    assemble { t.serializeContent(it) })
        }

        fun singleSig(publicKey: PublicKeyNim, signature: SignatureNim) =
            SignatureProof(publicKey, MerklePath(), signature)

        fun multiSig(signerKey: PublicKeyNim, publicKeys: Array<PublicKeyNim>, signature: SignatureNim) =
            SignatureProof(
                signerKey,
                MerklePath.compute(
                    publicKeys.map { it.hash },
                    signerKey.hash),
                signature
            )

        override fun serializedSize(o: SignatureProof): Int {
            TODO("not implemented: serializedSize")
        }

        override fun deserialize(s: InputStream) = SignatureProof(
            publicKey = s.read(PublicKeyNim()),
            merklePath = s.read(MerklePath),
            signature = s.read(SignatureNim())
        )

        override fun serialize(s: OutputStream, o: SignatureProof) = with(o) {
            s.write(publicKey)
            s.write(MerklePath, merklePath)
            if (signature != null)
                s.write(signature)
        }
    }

    fun verify(sender: Address?, data: ByteArray) =
        (
            (sender == null || isSignedBy(sender))
                &&
            signature?.verify(publicKey, data) ?: false
        )

    fun isSignedBy(sender: Address): Boolean {
        val merkleRoot = merklePath.computeRoot(publicKey.hash)
        val signerAddr = Address.fromHash(merkleRoot)
        return signerAddr == sender
    }

}