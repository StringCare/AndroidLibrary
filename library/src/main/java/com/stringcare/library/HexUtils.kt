package com.stringcare.library

import java.util.*

// https://gist.github.com/fabiomsr/845664a9c7e92bafb6fb0ca70d4e44fd

val ByteArray.asHexLower inline get() = this.joinToString(separator = "") { byte ->
    String.format("%02x", (byte.toInt() and 0xFF))
}

val ByteArray.asHexUpper inline get() = this.joinToString(separator = "") { byte ->
    String.format("%02X", (byte.toInt() and 0xFF))
}

val String.hexAsByteArray inline get() = this.chunked(2).map { string ->
    string.toUpperCase(Locale.getDefault()).toInt(16).toByte()
}.toByteArray()