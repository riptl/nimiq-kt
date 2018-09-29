package com.terorie.nimiq.consensus.block

import com.terorie.nimiq.DummyData
import com.terorie.nimiq.consensus.GenesisConfig
import org.junit.Test
import java.math.BigInteger
import kotlin.test.*

@ExperimentalUnsignedTypes
class BlockHeaderTest {

    private val prevHash = DummyData.hash1
    private val interlinkHash = DummyData.hash3
    private val bodyHash = DummyData.hash2
    private val accountsHash = DummyData.hash3
    private val difficulty = BlockUtils.difficultyToCompact(BigInteger.ONE)
    private val timestamp = 1U
    private val nonce = 1U

    @Test fun size() {
        val header = BlockHeader(prevHash, interlinkHash, bodyHash, accountsHash, difficulty, 1, timestamp, nonce)
        val isSize = BlockHeader.serializeToByteArray(header).size
        val reportSize = BlockHeader.serializedSize(header)
        val trueSize = 146

        assertEquals(trueSize, isSize)
        assertEquals(trueSize, reportSize)
    }

    @Test fun enc() {
        val srcHeader = BlockHeader(prevHash, interlinkHash, bodyHash, accountsHash, difficulty, 2, timestamp, nonce)
        val bytes = BlockHeader.serializeToByteArray(srcHeader)
        val gotHeader = BlockHeader.deserializeFromByteArray(bytes)

        assertEquals(srcHeader, gotHeader, "equals() method")
        assertEquals(srcHeader.prevHash, gotHeader.prevHash)
        assertEquals(srcHeader.bodyHash, gotHeader.bodyHash)
        assertEquals(srcHeader.accountsHash, gotHeader.accountsHash)
        assertEquals(srcHeader.difficulty, gotHeader.difficulty)
        assertEquals(srcHeader.height, gotHeader.height)
        assertEquals(srcHeader.timestamp, gotHeader.timestamp)
    }

    @Test fun invalidProofOfWork() {
        val header = BlockHeader(prevHash, interlinkHash, bodyHash, accountsHash, difficulty, 2, timestamp, nonce)
        assertFalse(header.verifyProofOfWork())
    }

    @Test fun validProofOfWork() {
        val header = GenesisConfig.genesisBlock.header
        assertTrue(header.verifyProofOfWork())
    }

}
