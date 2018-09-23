package com.terorie.nimiq

import java.math.BigInteger

@ExperimentalUnsignedTypes
object Policy {
    const val BLOCK_TIME = 60
    const val BLOCK_SIZE_MAX = 100000
    val BLOCK_TARGET_MAX = BigInteger.ONE shl 240
    const val DIFFICULTY_BLOCK_WINDOW = 120U
    const val DIFFICULTY_MAX_ADJUSTMENT_FACTOR = 2
    const val TRANSACTION_VALIDITY_WINDOW = 120U
    const val SATOSHIS_PER_COIN = 100000
    const val TOTAL_SUPPLY: Satoshi = 2100000000000000UL
    const val INITIAL_SUPPLY: Satoshi = 252000000000000UL
    const val EMISSION_SPEED = 4194304
    const val EMISSION_TAIL_START = 48692960
    const val EMISSION_TAIL_REWARD = 4000
    const val M = 240
    const val K = 120
    const val DELTA = 0.15
    const val NUM_BLOCKS_VERIFICATION = 250
    const val NUM_SNAPSHOTS_MAX = 20
}

