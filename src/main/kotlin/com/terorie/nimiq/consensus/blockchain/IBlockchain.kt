package com.terorie.nimiq.consensus.blockchain

import com.terorie.nimiq.consensus.block.Block
import com.terorie.nimiq.consensus.primitive.HashLight

@ExperimentalUnsignedTypes
interface IBlockchain {
    val head: Block
    var headHash: HashLight
    val height: UInt
}
