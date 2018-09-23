package com.terorie.nimiq

import java.io.InputStream
import java.io.OutputStream

data class PrunedAccount(val address: Address, val account: Account): Comparable<PrunedAccount> {

    companion object {
        fun unserialize(s: InputStream) = PrunedAccount(
            Address().apply{ unserialize(s) },
            Account.unserialize(s)
        )
    }

    fun serialize(s: OutputStream) {
        address.serialize(s)
        account.serialize(s)
    }

    override fun compareTo(other: PrunedAccount) =
        address.compareTo(other.address)

}