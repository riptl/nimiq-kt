package com.terorie.nimiq

class PublicKeyNim : Blob(32) {

    fun toAddress() = Address.fromHash(hash())

}
