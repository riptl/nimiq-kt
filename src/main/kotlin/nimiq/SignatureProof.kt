package com.terorie.nimiq

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

class SignatureProof(
        val publicKey: PublicKeyNim,
        val merklePath: MerklePath,
        val signature: SignatureNim?
) {

    companion object {
        fun verifyTransaction(t: Transaction): Boolean {
            val buffer = t.proof
            val inStream = ByteArrayInputStream(buffer)
            val proof = unserialize(inStream)

            // Reject proof if it is longer than needed.
            if (inStream.available() > 0)
                return false

            return proof.verify(t.sender, t.serializeContent())
        }

        fun singleSig(publicKey: PublicKeyNim, signature: SignatureNim) =
            SignatureProof(publicKey, MerklePath(), signature)

        fun multiSig(signerKey: PublicKeyNim, publicKeys: Array<PublicKeyNim>, signature: SignatureNim) =
            SignatureProof(
                signerKey,
                MerklePath.compute(
                    publicKeys.map{it.hash()},
                    signerKey.hash()),
                signature
            )

        fun unserialize(s: InputStream) = SignatureProof(
            PublicKeyNim().apply{ unserialize(s) },
            MerklePath.unserialize(s),
            SignatureNim().apply{ unserialize(s) }
        )
    }

    fun verify(sender: Address?, data: ByteArray) =
        (
            (sender == null || isSignedBy(sender))
                &&
            signature?.verify(publicKey, data) ?: false
        )

    fun isSignedBy(sender: Address): Boolean {
        val merkleRoot = merklePath.computeRoot(publicKey.hash())
        val signerAddr = Address.fromHash(merkleRoot)
        return signerAddr == sender
    }

    fun serialize(b: OutputStream) {
        publicKey.serialize(b)
        merklePath.serialize(b)
        signature?.serialize(b)
    }

}