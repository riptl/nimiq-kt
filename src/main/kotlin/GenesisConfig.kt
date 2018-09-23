object GenesisConfig {

    var initialized = false
    lateinit var networkID: UByte
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
        networkID = 42
        networkName = "main"
        
    }

}
