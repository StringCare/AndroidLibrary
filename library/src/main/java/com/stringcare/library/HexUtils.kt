package com.stringcare.library

import org.apache.commons.lang3.StringEscapeUtils
import java.util.*

// https://gist.github.com/fabiomsr/845664a9c7e92bafb6fb0ca70d4e44fd

val ByteArray.asHexLower inline get() = this.joinToString(separator = ":") { byte ->
    String.format("%02x", (byte.toInt() and 0xFF))
}

val ByteArray.asHexUpper inline get() = this.joinToString(separator = ":") { byte ->
    String.format("%02X", (byte.toInt() and 0xFF))
}

val String.hexAsByteArray inline get() = this.chunked(2).map { string ->
    string.toUpperCase(Locale.getDefault()).toInt(16).toByte()
}.toByteArray()

fun String.escape(): String = Regex.escape(this)
fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
fun String.removeNewLines(): String = this.replace("\n", "")
fun String.androidTreatment(): String {
    val va = this.split(" ")
    val values = mutableListOf<String>()
    va.forEach { value ->
        if (value.trim().isNotBlank()) {
            values.add(value.trim())
        }
    }
    return values.joinToString(separator = " ")
}