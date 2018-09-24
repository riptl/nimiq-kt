package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
data class PrunedAccount(val address: Address, val account: Account): Comparable<PrunedAccount> {

    companion object {
        fun unserialize(s: InputStream) = PrunedAccount(
                Address().apply { unserialize(s) },
                Account.unserialize(s)
        )
    }

    fun serialize(s: OutputStream) {
        address.serialize(s)
        account.serialize(s)
    }

    var _hash: HashLight? = null
    val hash: HashLight
        get() {
            if (_hash == null)
                _hash = HashLight(assemble { serialize(it) })
            return _hash!!
        }

    override fun compareTo(other: PrunedAccount) =
        address.compareTo(other.address)

}