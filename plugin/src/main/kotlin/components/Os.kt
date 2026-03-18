package components

enum class Os {
    WINDOWS,
    OSX
}

fun getOs(): Os = when {
    System.getProperty("os.name").toLowerCase().contains("windows") -> Os.WINDOWS
    else -> Os.OSX
}
