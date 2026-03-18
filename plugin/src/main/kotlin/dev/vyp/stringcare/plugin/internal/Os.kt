package dev.vyp.stringcare.plugin.internal

enum class Os {
    WINDOWS,
    OSX
}

fun getOs(): Os = when {
    System.getProperty("os.name").lowercase().contains("windows") -> Os.WINDOWS
    else -> Os.OSX
}
