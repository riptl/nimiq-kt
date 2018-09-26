package com.terorie.nimiq.consensus.account

@ExperimentalUnsignedTypes
class AccountsTreeStore {

    fun get(key: String): AccountsTreeNode? = TODO()
    fun put(node: AccountsTreeNode): String = TODO()
    fun remove(node: AccountsTreeNode): String = TODO()

    fun getRootNode() = get("")

}
