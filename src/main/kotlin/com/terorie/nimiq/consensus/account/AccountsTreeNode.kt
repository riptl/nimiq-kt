package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@ExperimentalUnsignedTypes
abstract class AccountsTreeNode(val prefix: String) {

    companion object : Enc<AccountsTreeNode> {
        override fun deserialize(s: InputStream): AccountsTreeNode {
            val type = s.readUByte()
            val prefix = s.readVarString()

            return when (type.toInt()) {
                0x00 -> AccountsTreeBranch.deserializeContent(prefix, s)
                0xFF -> AccountsTreeTerminal.deserializeContent(prefix, s)
                else -> throw IllegalArgumentException("Invalid AccountsTreeNode type: $type")
            }
        }

        override fun serialize(s: OutputStream, o: AccountsTreeNode) = when(o) {
            is AccountsTreeBranch -> AccountsTreeBranch.serialize(s, o)
            is AccountsTreeTerminal -> AccountsTreeTerminal.serialize(s, o)
            else -> throw IllegalStateException("neither terminal nor branch")
        }

        override fun serializedSize(o: AccountsTreeNode) = when(o) {
            is AccountsTreeBranch -> AccountsTreeBranch.serializedContentSize(o)
            is AccountsTreeTerminal -> AccountsTreeTerminal.SIZE
            else -> throw IllegalArgumentException("neither terminal nor branch")
        } + varStringSize(o.prefix)
    }

    protected var _hash: HashLight? = null
    val hash: HashLight get() {
        if (_hash == null)
            _hash = HashLight(assemble { serialize(it, this) })
        return _hash!!
    }

    abstract val hasSingleChild: Boolean
    abstract val hasChildren: Boolean

}

@ExperimentalUnsignedTypes
class AccountsTreeBranch(
    prefix: String,
    val childSuffixes: Array<String?> = emptyArray(),
    val childHashes: Array<HashLight?> = emptyArray()
) : AccountsTreeNode(prefix) {

    companion object {
        fun serializedContentSize(o: AccountsTreeBranch): Int {
            var i = 1
            for (suffix in o.childSuffixes)
                if (suffix != null)
                    i += HashLight.SIZE + varStringSize(suffix)
            return i
        }

        fun deserializeContent(prefix: String, s: InputStream): AccountsTreeBranch {
            val childCount = s.readUByte().toInt()
            val childSuffixes = arrayOfNulls<String>(childCount)
            val childHashes = arrayOfNulls<HashLight>(childCount)
            for (i in 0 until childCount) {
                val childSuffix = s.readVarString()
                val ci = childSuffix[0].toString().toInt(16)
                childSuffixes[ci] = childSuffix
                childHashes[ci] = s.read(HashLight())
            }
            return AccountsTreeBranch(prefix, childSuffixes, childHashes)
        }

        fun serialize(s: OutputStream, o: AccountsTreeBranch) = with(o) {
            s.writeUByte(0x00)
            s.writeVarString(prefix)
            val childCount = 0 // TODO
            s.writeUByte(childCount)
            for (i in 0 until childSuffixes.size) {
                if (childSuffixes[i] != null)
                    s.writeVarString(childSuffixes[i]!!)
                s.write(childHashes[i]!!)
            }
        }
    }

    private fun getChildIndex(prefix: String): Int {
        if (prefix.substring(0..this.prefix.length) == prefix)
            throw IllegalArgumentException("Prefix $prefix is not a child of the current node ${this.prefix}")
        return prefix[this.prefix.length].toString().toInt(16)
    }

    fun withChild(prefix: String, childHash: HashLight): AccountsTreeBranch {
        // TODO Allocate space for new child
        val childSuffixes = childSuffixes.copyOf()
        val childHashes = childHashes.copyOf()
        val index = getChildIndex(prefix)
        childSuffixes[index] = prefix
        childHashes[index] = childHash
        return AccountsTreeBranch(prefix, childSuffixes, childHashes)
    }

    fun withoutChild(prefix: String): AccountsTreeBranch {
        // TODO Allocate space for new child
        val childSuffixes = childSuffixes.copyOf()
        val childHashes = childHashes.copyOf()
        val index = getChildIndex(prefix)
        childSuffixes[index] = null
        childHashes[index] = null
        return AccountsTreeBranch(prefix, childSuffixes, childHashes)
    }

    override val hasSingleChild: Boolean get() {
        var count = 0
        for (suffix in childSuffixes)
            if (suffix != null)
                count++
        return count == 1
    }

    override val hasChildren =
        childSuffixes.find { it != null } != null

    fun getFirstChild(): String? {
        val suffix = childSuffixes.first { it != null }
            ?: return null
        return prefix + suffix
    }

}

@ExperimentalUnsignedTypes
class AccountsTreeTerminal(
    prefix: String,
    val account: Account
) : AccountsTreeNode(prefix) {

    companion object {
        const val SIZE = Account.SIZE

        fun deserializeContent(prefix: String, s: InputStream) =
            AccountsTreeTerminal(prefix, Account.unserialize(s))

        fun serialize(s: OutputStream, o: AccountsTreeTerminal) =
            o.account.serialize(s)
    }

    override val hasSingleChild = false
    override val hasChildren = false

}
