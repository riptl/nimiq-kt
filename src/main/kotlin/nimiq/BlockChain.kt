package com.terorie.nimiq

@ExperimentalUnsignedTypes
class BlockChain(val blocks: ArrayList<Block>) {

    fun verify(): Boolean {
        // For performance reasons, we DO NOT VERIFY the validity of the blocks in the chain here.
        // Block validity is checked by the Nano/LightChain upon receipt of a ChainProof.

        // Check that all blocks in the chain are valid successors of one another.
        for (i in blocks.size - 1 downTo 1)
            if (!blocks[i].isSuccessorOf(blocks[i - 1]))
                return false

        // Everything checks out.
        return true
    }

    fun denseSuffix(): List<Block> {
        // Compute the dense suffix
        val denseSuffix = arrayListOf(head)
        var denseSuffixHead = head
        for(i in blocks.size - 2 downTo 0) {
            val block = blocks[i]
            val hash = block.header.hash
            if (hash != denseSuffixHead.header.prevHash)
                break
            denseSuffix.add(block)
            denseSuffixHead = block
        }
        denseSuffix.reverse()
        return denseSuffix
    }

    val isAnchored get() =
        GenesisConfig.genesisHash == tail.header.hash

    val length get() = blocks.size

    val head get() = blocks.last()
    val tail get() = blocks.first()

}
