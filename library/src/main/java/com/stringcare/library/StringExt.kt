package com.stringcare.library

import org.json.JSONArray
import org.json.JSONObject

fun Int.string(): String = SC.context.getString(this)

fun Int.reveal(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version)

fun Int.reveal(
        vararg formatArgs: Any,
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version, formatArgs)

fun String.obfuscate(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.obfuscate(this, androidTreatment, version)

fun String.reveal(
        androidTreatment: Boolean = defaultAndroidTreatment,
        version: Version = defaultVersion
): String = SC.reveal(this, androidTreatment, version)

fun String.json(
        predicate: () -> Boolean = { true }
): JSONObject = SC.asset().json(this, predicate)

fun String.asyncJson(
        predicate: () -> Boolean = { true },
        json: (json: JSONObject) -> Unit
) = SC.asset().asyncJson(this, predicate, json)

fun String.jsonArray(
        predicate: () -> Boolean = { true }
): JSONArray = SC.asset().jsonArray(this, predicate)

fun String.asyncJsonArray(
        predicate: () -> Boolean = { true },
        json: (json: JSONArray) -> Unit
) = SC.asset().asyncJsonArray(this, predicate, json)

fun String.bytes(
        predicate: () -> Boolean = { true }
): ByteArray = SC.asset().bytes(this, predicate)

fun String.asyncBytes(
        predicate: () -> Boolean = { true },
        bytes: (bytes: ByteArray) -> Unit
) = SC.asset().asyncBytes(this, predicate, bytes)
