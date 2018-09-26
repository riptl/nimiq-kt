package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream

@ExperimentalUnsignedTypes
data class PrunedAccount(val address: Address, val account: Account): Comparable<PrunedAccount> {

    companion object : Enc<PrunedAccount> {
        override fun serializedSize(o: PrunedAccount): Int {
            var size = 20 // Address
            size += Account.serializedSize(o.account)
            return size
        }

        override fun deserialize(s: InputStream) = PrunedAccount(
            s.read(Address()),
            s.read(Account)
        )

        override fun serialize(s: OutputStream, o: PrunedAccount) = with(o) {
            s.write(address)
            s.write(Account, account)
        }
    }

    fun serialize(s: OutputStream) {

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