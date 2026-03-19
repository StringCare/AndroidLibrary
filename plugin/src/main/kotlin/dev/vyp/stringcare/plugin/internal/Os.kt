package dev.vyp.stringcare.plugin.internal

enum class Os {
    WINDOWS,
    OSX,
    LINUX,
}

fun getOs(): Os {
    val name = System.getProperty("os.name").lowercase()
    return when {
        name.contains("windows") -> Os.WINDOWS
        name.contains("linux") || name.contains("freebsd") -> Os.LINUX
        else -> Os.OSX
    }
}
