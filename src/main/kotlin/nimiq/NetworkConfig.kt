package com.terorie.nimiq

@ExperimentalUnsignedTypes
abstract class NetworkConfig {

    abstract val protocol: Protocol
    abstract val secure: Boolean

}

@ExperimentalUnsignedTypes
class WssNetworkConfig(
    val host: String,
    val port: Int,
    val key: String?,
    val cert: String?
) : NetworkConfig() {

    override val protocol = Protocol.WSS
    override val secure = true

}
