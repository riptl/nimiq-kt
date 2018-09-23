object MerkleTree {

    fun computeRoot(values: List<HashLight>): HashLight =
            compute(values)

    private fun compute(values: List<HashLight>): HashLight = when(values.size) {
        0 -> HashLight(ByteArray(0))
        1 -> values[0]
        else -> {
            val mid = values.size / 2
            val left = values.slice(0..mid)
            val right = values.slice(mid..values.size)
            val leftHash = compute(left)
            val rightHash = compute(right)
            HashLight(leftHash.buf, rightHash.buf)
        }
    }

}