package dev.vyp.stringcare.library

import org.json.JSONObject

interface JSONObjectListener : AssetListener {
    fun assetReady(json: JSONObject)
}
