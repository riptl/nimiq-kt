package com.terorie.nimiq

@ExperimentalUnsignedTypes
abstract class Blockchain {
    abstract val head: Block
    abstract val headHash: HashLight
    abstract val height: UInt
}
