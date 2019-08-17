package com.stringcare.library

interface AssetByteArrayListener : AssetListener {
    fun assetReady(byteArray: ByteArray)
}
