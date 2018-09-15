class Hash(val algorithm: Algorithm) : Blob(algorithm.size) {

    enum class Algorithm(val size: Int) {
        INVALID(-1),
        BLAKE2B(32),
        ARGON2D(32),
        SHA256(32),
        SHA512(64)
    }

    override fun equals(other: Any?) =
        if (other is Hash)
            algorithm == other.algorithm &&
            super.equals(other)
        else false

}