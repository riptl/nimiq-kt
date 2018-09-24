package com.terorie.nimiq

@ExperimentalUnsignedTypes
class Network(
        val blockchain: Blockchain,
        val networkConfig: NetworkConfig,
        val time: UInt
)
