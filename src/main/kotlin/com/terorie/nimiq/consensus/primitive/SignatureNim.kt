package com.terorie.nimiq.consensus.primitive

import com.terorie.nimiq.util.Blob
import org.bouncycastle.math.ec.rfc8032.Ed25519

class SignatureNim : Blob(64) {

    fun verify(publicKey: PublicKeyNim, data: ByteArray) =
        Ed25519.verify(buf, 0, publicKey.buf, 0, data, 0, data.size)

}
