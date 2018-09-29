package com.terorie.nimiq.consensus.account

@ExperimentalUnsignedTypes
open class AccountsTreeStore {

    open fun get(key: String): AccountsTreeNode? = TODO()
    open fun put(node: AccountsTreeNode): String = TODO()
    open fun remove(node: AccountsTreeNode): String = TODO()

    open fun getRootNode() = get("")

}
