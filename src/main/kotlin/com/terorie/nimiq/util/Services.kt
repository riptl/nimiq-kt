package com.terorie.nimiq.util

@ExperimentalUnsignedTypes
object Services {
    const val NONE = 0U
    const val NANO = 1U
    const val LIGHT = 2U
    const val FULL = 4U

    fun isNanoNode(s: UInt) = s and NANO != 0U
    fun isLightNode(s: UInt) = s and LIGHT != 0U
    fun isFullNode(s: UInt) = s and FULL != 0U
}
