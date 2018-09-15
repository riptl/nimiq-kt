import org.bouncycastle.crypto.digests.Blake2bDigest

class HashLight() : Blob(SIZE) {

    companion object {
        const val SIZE = 32
    }

    constructor(input: ByteArray) : this() {
        val d = Blake2bDigest()
        d.update(input, 0, input.size)
        d.doFinal(buf, 0)
    }

}
