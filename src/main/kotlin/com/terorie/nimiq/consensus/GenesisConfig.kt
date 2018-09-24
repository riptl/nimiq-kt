package com.terorie.nimiq.consensus

import com.terorie.nimiq.consensus.primitive.PublicKeyNim
import com.terorie.nimiq.network.address.SeedListURL
import com.terorie.nimiq.consensus.account.Address
import com.terorie.nimiq.consensus.block.*
import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.network.Protocol
import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.network.address.WsBasePeerAddress
import org.bouncycastle.util.encoders.Base64
import java.math.BigInteger
import java.net.URL

@ExperimentalUnsignedTypes
object GenesisConfig {

    var initialized = false
    var networkID: UByte = 0U
    lateinit var networkName: String
    lateinit var seedPeers: ArrayList<PeerAddress>
    lateinit var seedLists: ArrayList<SeedListURL>
    lateinit var genesisBlock: Block
    lateinit var genesisHash: HashLight
    lateinit var genesisAccounts: ByteArray

    fun init() {
        if (initialized)
            throw IllegalStateException("GenesisConfig already initialized")
        initialized = true
    }

    fun main() {
        networkID = 42U
        networkName = "main"
        val seed = WsBasePeerAddress.Companion::seed
        val wss = Protocol.WSS
        seedPeers = arrayListOf(
            seed(wss, "seed-1.nimiq.com", 8443, "b70d0c3e6cdf95485cac0688b086597a5139bc4237173023c83411331ef90507"),
            seed(wss, "seed-2.nimiq.com", 8443, "8580275aef426981a04ee5ea948ca3c95944ef1597ad78db9839f810d6c5b461"),
            seed(wss, "seed-3.nimiq.com", 8443, "136bdec59f4d37f25ac8393bef193ff2e31c9c0a024b3edbf77fc1cb84e67a15"),
            seed(wss, "seed-4.nimiq-network.com", 8443, "aacf606335cdd92d0dd06f27faa3b66d9bac0b247cd57ade413121196b72cd73"),
            seed(wss, "seed-5.nimiq-network.com", 8443, "110a81a033c75976643d4b8f34419f4913b306a6fc9d530b8207ddbd5527eff6"),
            seed(wss, "seed-6.nimiq-network.com", 8443, "26c1a4727cda6579639bdcbaecb1f6b97be3ac0e282b43bdd1a2df2858b3c23b"),
            seed(wss, "seed-7.nimiq.network", 8443, "82fcebdb4e2a7212186d1976d7f685cc86cdf58beffe1723d5c3ea5be00c73e1"),
            seed(wss, "seed-8.nimiq.network", 8443, "b7ac8cc1a820761df4e8a42f4e30c870e81065c4e29f994ebb5bdceb48904e7b"),
            seed(wss, "seed-9.nimiq.network", 8443, "4429bf25c8d296c0f1786647d8f7d4bac40a37c67caf028818a65a9cc7865a48"),
            seed(wss, "seed-10.nimiq.network", 8443, "e8e99fb8633d660d4f2d48edb6cc294681b57648b6ec6b28af8f85b2d5ec4e68"),
            seed(wss, "seed-11.nimiq.network", 8443, "a76f0edabacfe701750036bad473ff92fa0e68ef655ab93135f0572af6e5baf8"),
            seed(wss, "seed-12.nimiq.network", 8443, "dca57704191306ac1315e051b6dfef6c174fb2af011a52a3d922fbfaec2be41a"),
            seed(wss, "seed-13.nimiq-network.com", 8443, "30993f92f148da125a6f8bc191b3e746fab39e109220daa0966bf6432e909f3f"),
            seed(wss, "seed-14.nimiq-network.com", 8443, "6e7f904fabfadb194d6c74b16534bacb69892d80909cf959e47d3c8f5f330ad2"),
            seed(wss, "seed-15.nimiq-network.com", 8443, "7cb662a686144c17ae4153fbf7ce359f7e9da39dc072eb11092531f9104fbdf6"),
            seed(wss, "seed-16.nimiq.com", 8443, "0dfd11939947101197e3c3768a086e65ef1e893e71bfcf4bd5ed222957825212"),
            seed(wss, "seed-17.nimiq.com", 8443, "c7120f4f88b70a38daa9783e30e89c1c55c3d80d0babed44b4e2ddd09052664a"),
            seed(wss, "seed-18.nimiq.com", 8443, "c15a2d824a52837fa7165dc232592be35116661e7f28605187ab273dd7233711"),
            seed(wss, "seed-19.nimiq.com", 8443, "98a24d4b05158314b36e0bd6ce3b42ac5ac061f4bb9664d783eb930caa9315b6"),
            seed(wss, "seed-20.nimiq.com", 8443, "1fc33f93273d94dd2cf7470274c27ecb1261ec983e43bdbb281803c0a09e68d5")
        )
        seedLists = arrayListOf(
                SeedListURL(
                        url = URL("https://nimiq.community/seeds.txt"),
                        publicKey = PublicKeyNim.fromHex("8b4ae04557f490102036ce3e570b39058c92fc5669083fb9bbb6effc91dc3c71")
                )
        )
        genesisBlock = Block(
                BlockHeader(
                        prevHash = HashLight(),
                        interlinkHash = HashLight(),
                        bodyHash = HashLight.fromBase64("fNqaf98GZVkFrl29nFNUUUcbB4+m898OKH5bD7R6Vzo="),
                        accountsHash = HashLight.fromBase64("H+/UTx+pcYX9oh6VdUXJfcdkP6fk792G4KpCRNHgvFw="),
                        nBits = BlockUtils.difficultyToCompact(BigInteger.ONE),
                        height = 1U,
                        timestamp = 1523727000U,
                        nonce = 137689U,
                        version = 1U
                ),
                BlockInterlink(
                        hashes = arrayListOf(),
                        prevHash = HashLight(),
                        repeatBits = byteArrayOf(),
                        compressed = arrayListOf()
                ),
                BlockBody(
                        minerAddr = Address(),
                        transactions = arrayListOf(),
                        extraData = Base64.decode("bG92ZSBhaSBhbW9yIG1vaGFiYmF0IGh1YnVuIGNpbnRhIGx5dWJvdiBiaGFsYWJhc2EgYW1vdXIga2F1bmEgcGknYXJhIGxpZWJlIGVzaHEgdXBlbmRvIHByZW1hIGFtb3JlIGthdHJlc25hbiBzYXJhbmcgYW5wdSBwcmVtYSB5ZXU=")
                )
        )
        genesisAccounts = loadResource("genesis_accounts_main.bin")
    }

    fun test() {
        networkID = 1U
        networkName = "test"
        val seed = WsBasePeerAddress.Companion::seed
        val wss = Protocol.WSS
        seedPeers = arrayListOf(
            seed(wss, "seed1.nimiqtest.net", 8080, "175d5f01af8a5911c240a78df689a76eef782d793ca15d073bdc913edd07c74b"),
            seed(wss, "seed2.nimiqtest.net", 8080, "2c950d2afad1aa7ad12f01a56527f709b7687b1b00c94da6e0bd8ae4d263d47c"),
            seed(wss, "seed2.nimiqtest.net", 8080, "03feec9d5316a7b5ebb69c4e709547a28afe8e9ef91ee568df489d29e9845bb8"),
            seed(wss, "seed2.nimiqtest.net", 8080, "943d5669226d3716a830371d99143af98bbaf84c630db24bdd67e55ccb7a9011")
        )
        seedLists = arrayListOf()
        genesisBlock = Block(
                BlockHeader(
                        prevHash = HashLight(),
                        interlinkHash = HashLight(),
                        bodyHash = HashLight.fromBase64("9rorv34UeKIJBXAARx1z+9wo3wtxd0fZKc/egpxBIPY="),
                        accountsHash = HashLight.fromBase64("LgLaPRYuIPqYICnb3pzCD2tDGrBd8XZPNK9MYqTysz8="),
                        nBits = BlockUtils.difficultyToCompact(BigInteger.ONE),
                        height = 1U,
                        timestamp = 1522735199U,
                        nonce = 79001U,
                        version = 1U
                ),
                BlockInterlink(
                        hashes = arrayListOf(),
                        prevHash = HashLight(),
                        repeatBits = byteArrayOf(),
                        compressed = arrayListOf()
                ),
                BlockBody(
                        minerAddr = Address(),
                        transactions = arrayListOf(),
                        extraData = Base64.decode("bG92ZSBhaSBhbW9yIG1vaGFiYmF0IGh1YnVuIGNpbnRhIGx5dWJvdiBiaGFsYWJhc2EgYW1vdXIga2F1bmEgcGknYXJhIGxpZWJlIGVzaHEgdXBlbmRvIHByZW1hIGFtb3JlIGthdHJlc25hbiBzYXJhbmcgYW5wdSBwcmVtYSB5ZXU=")
                )
        )
        genesisAccounts = loadResource("genesis_accounts_test.bin")
    }

    fun dev() {
        networkID = 2U
        networkName = "dev"
        seedPeers = arrayListOf(
            WsBasePeerAddress.seed(Protocol.WS, "dev.nimiq-network.com", 8080, "e65e39616662f2c16d62dc08915e5a1d104619db8c2b9cf9b389f96c8dce9837")
        )
        seedLists = arrayListOf()
        genesisBlock = Block(
                BlockHeader(
                        prevHash = HashLight(),
                        interlinkHash = HashLight(),
                        bodyHash = HashLight.fromBase64("JvMr9c9l2m8HWNdFAGTEastKH+aDZvln9EopXelhVIg="),
                        accountsHash = HashLight.fromBase64("1t/Zm91tN0p178+ePcxyR5bPxvC6jFLskqiidFFO3wY="),
                        nBits = BlockUtils.difficultyToCompact(BigInteger.ONE),
                        height = 1U,
                        timestamp = 1522338300U,
                        nonce = 12432U,
                        version = 1U
                ),
                BlockInterlink(
                        hashes = arrayListOf(),
                        prevHash = HashLight(),
                        repeatBits = byteArrayOf(),
                        compressed = arrayListOf()
                ),
                BlockBody(
                        minerAddr = Address(),
                        transactions = arrayListOf(),
                        extraData = Base64.decode("RGV2TmV0")
                )
        )
        genesisAccounts = loadResource("genesis_accounts_dev.bin")
    }

    fun bounty() {
        networkID = 3U
        networkName = "bounty"
        val seed = WsBasePeerAddress.Companion::seed
        val wss = Protocol.WSS
        seedPeers = arrayListOf(
            seed(wss, "bug-bounty1.nimiq-network.com", 8080, "7e825872ee12a71bda50cba9f230c760c84ee50eef0a3e435467e8d5307c0b4e"),
            seed(wss, "bug-bounty2.nimiq-network.com", 8080, "ea876175c8b693c0db38b7c17d66e9c510020fceb4634f04e281af30438f8787"),
            seed(wss, "bug-bounty3.nimiq-network.com", 8080, "5c0d5d801e85ebd42f25a45b2cb7f3b39b9ce14002d4662f5ed0cd79ce25165a")
        )
        seedLists = arrayListOf()
        genesisBlock = Block(
                BlockHeader(
                        prevHash = HashLight(),
                        interlinkHash = HashLight(),
                        bodyHash = HashLight.fromBase64("nPcJa/7i0KYsiPQ8FPOgvLYgpP3m05UMwPfIPJAdAvI="),
                        accountsHash = HashLight.fromBase64("sXZsIZDV40vD7NDdrnSk2tOsPMKKit/vH0xvz1RXmQo="),
                        nBits = BlockUtils.difficultyToCompact(BigInteger.ONE),
                        height = 1U,
                        timestamp = 1522338300U,
                        nonce = 67058U,
                        version = 1U
                ),
                BlockInterlink(
                        hashes = arrayListOf(),
                        prevHash = HashLight(),
                        repeatBits = byteArrayOf(),
                        compressed = arrayListOf()
                ),
                BlockBody(
                        minerAddr = Address(),
                        transactions = arrayListOf(),
                        extraData = Base64.decode("Qm91bnR5TmV0")
                )
        )
        genesisAccounts = loadResource("genesis_accounts_bounty.bin")
    }

    private fun loadResource(path: String) =
        javaClass.getResource(path).readBytes()

}
