package com.terorie.nimiq

@ExperimentalUnsignedTypes
class NanoChain(var time: UInt) : BaseChain(ChainDataStore.createVolatile()) {

    companion object {
        const val ERR_ORPHAN = -2
        const val ERR_INVALID = -1
        const val OK_KNOWN = 0
        const val OK_EXTENDED = 1
        const val OK_REBRANCHED = 2
        const val OK_FORKED = 3
        const val SYNCHRONIZER_THROTTLE_AFTER = 500 // ms
        const val SYNCHRONIZER_THROTTLE_WAIT = 30 // ms
    }

    init {
        headHash = GenesisConfig.genesisHash
    }

    private fun pushProof(proof: ChainProof) {

    }

}
