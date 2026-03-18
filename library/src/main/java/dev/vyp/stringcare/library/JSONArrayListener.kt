package dev.vyp.stringcare.library

import org.json.JSONArray

interface JSONArrayListener : AssetListener {
    fun assetReady(json: JSONArray)
}
