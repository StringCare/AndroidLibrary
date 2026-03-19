package dev.vyp.stringcare.plugin.domain.models

import java.io.File

data class AssetsFile(
    val file: File,
    val sourceFolder: String,
    val module: String,
) {
    /**
     * Path relative to the `assets/` directory for writing generated assets.
     */
    fun relativePathUnderAssets(): String {
        val path = file.absolutePath.replace('\\', '/')
        val marker = "/assets/"
        val idx = path.indexOf(marker)
        require(idx >= 0) { "Expected .../assets/... in ${file.absolutePath}" }
        return path.substring(idx + marker.length)
    }
}
