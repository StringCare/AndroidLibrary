package dev.vyp.stringcare.library

interface AssetByteArrayListener : AssetListener {
    fun assetReady(byteArray: ByteArray)
}
