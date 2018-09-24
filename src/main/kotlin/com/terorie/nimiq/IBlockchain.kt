package com.terorie.nimiq

@ExperimentalUnsignedTypes
interface IBlockchain {
    val head: Block
    var headHash: HashLight
    val height: UInt
}
