interface Blockchain {
    val head: Block
    val headHash: HashLight
    val height: UInt
}
