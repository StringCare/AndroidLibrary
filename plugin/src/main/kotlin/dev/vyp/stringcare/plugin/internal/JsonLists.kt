package dev.vyp.stringcare.plugin.internal

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private val jsonParser =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

internal fun toJsonStringList(list: List<String>): String =
    jsonParser.encodeToString(ListSerializer(String.serializer()), list)

/** Parses a JSON array of strings (e.g. Gson-produced lists in task inputs). */
internal fun parseJsonStringList(json: String): List<String> =
    try {
        val element = jsonParser.parseToJsonElement(json.ifBlank { "[]" })
        if (element is JsonArray) {
            element.map { it.jsonPrimitive.content }
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }
