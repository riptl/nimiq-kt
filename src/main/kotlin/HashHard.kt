class HashHard() : Blob(SIZE) {

    companion object {
        const val SIZE = 32
    }

    constructor(input: ByteArray): this() {
        Native.nimiqArgon2d(buf, input)
    }

    fun toGeneric() = Hash(Hash.Algorithm.ARGON2D)

}
