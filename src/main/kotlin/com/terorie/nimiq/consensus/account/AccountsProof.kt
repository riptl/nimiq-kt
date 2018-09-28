package com.terorie.nimiq.consensus.account

import com.terorie.nimiq.consensus.primitive.HashLight
import com.terorie.nimiq.util.io.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
class AccountsProof(val nodes: Array<AccountsTreeNode>) {

    companion object : Enc<AccountsProof> {

        override fun serializedSize(o: AccountsProof): Int {
            TODO("not implemented: serializedSize")
        }

        override fun deserialize(s: InputStream): AccountsProof =
            AccountsProof(Array(size = s.readUShort().toInt()) {
                s.read(AccountsTreeNode)
            })

        override fun serialize(s: OutputStream, o: AccountsProof) = with(o) {
            s.writeUShort(nodes.size.toUShort())
            for (node in nodes)
                s.write(AccountsTreeNode, node)
        }

    }

    val index = HashMap<HashLight, AccountsTreeNode>()

    fun verify(): Boolean {
        // TODO Use Stack
        val children = ArrayList<AccountsTreeNode>()
        index.clear()
        for (node in nodes) {
            // If node is a branch node, validate its children
            if (node is AccountsTreeBranch) {
                while (children.isNotEmpty()) {
                    val child = children.last()
                    if (child.isChildOf(node)) {
                        // If the child is not valid, return false
                        if (node.getChildHash(child.prefix) != child.hash
                            || node.getChild(child.prefix) != child.prefix)
                            return false

                        children.removeAt(children.lastIndex)
                        index[child.hash] = child
                    } else {
                        break
                    }
                }
            }

            children.add(node)
        }

        // The last element must be the root node.
        return children.size == 1
            && children[0].prefix == ""
            && children[0] is AccountsTreeBranch
    }

    fun getAccount(address: Address): Account? {
        if (index.isEmpty())
            throw IllegalArgumentException("verify() must be called once before using getAccount()")

        val rootNode = nodes.last()
        val prefix = address.toHex()

        return doGetAccount(rootNode, prefix)
    }

    private fun doGetAccount(node: AccountsTreeNode, prefix: String): Account? {
        // Find common prefix between node and requested address.
        val commonPrefix = node.prefix.commonPrefixWith(prefix)

        // If the prefix does not fully match, the requested account does not exist.
        if (commonPrefix.length != node.prefix.length)
            return null

        // If the remaining address is empty, we have found the requested node.
        if (commonPrefix == prefix)
            return (node as AccountsTreeTerminal).account

        // Descend into the matching child node if one exists.
        if (node is AccountsTreeBranch) {
            // If no matching child exists, the requested account does not exist.
            val childKey = node.getChildHash(prefix)
                ?: return null

            // If the child exists but is not part of the proof, fail.
            val childNode = index[childKey]
                ?: throw IllegalArgumentException("Requested address not part of AccountsProof")

            return doGetAccount(childNode, prefix)
        } else {
            return null
        }
    }

    val length = nodes.size

}
