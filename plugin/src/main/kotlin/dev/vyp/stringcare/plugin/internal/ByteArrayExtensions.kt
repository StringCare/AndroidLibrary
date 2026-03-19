package dev.vyp.stringcare.plugin.internal

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toReadableString(): String = joinToString(separator = ", ") { it.toString() }
