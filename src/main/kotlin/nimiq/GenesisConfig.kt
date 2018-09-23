package com.terorie.nimiq

@ExperimentalUnsignedTypes
object GenesisConfig {

    var initialized = false
    var networkID: UByte = 0U
    lateinit var networkName: String
    lateinit var seedPeers: ArrayList<WssPeerAddress>
    lateinit var seedLists: ArrayList<SeedListURL>
    lateinit var genesisBlock: Block
    lateinit var genesisHash: HashLight

    fun init() {
        if (initialized)
            throw IllegalStateException("GenesisConfig already initialized")
        initialized = true
    }

    fun main() {
        networkID = 42U
        networkName = "main"
        
    }

}
