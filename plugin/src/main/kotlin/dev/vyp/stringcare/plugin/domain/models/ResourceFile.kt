package dev.vyp.stringcare.plugin.domain.models

import java.io.File

/**
 * String XML resource file selected for obfuscation.
 *
 * Example:
 * `ResourceFile(File("src/main/res/values/strings.xml"), "src/main", "app")`
 *
 * @property file Physical file in the project.
 * @property sourceFolder Source-set root used to resolve paths.
 * @property module Gradle module name that owns the resource file.
 */
data class ResourceFile(
    val file: File,
    val sourceFolder: String,
    val module: String,
) {
    /**
     * Path relative to the `res/` directory (e.g. `values/strings.xml`) for writing generated resources.
     */
    fun relativePathUnderRes(): String {
        val path = file.absolutePath.replace('\\', '/')
        val marker = "/res/"
        val idx = path.indexOf(marker)
        require(idx >= 0) { "Expected .../res/... in ${file.absolutePath}" }
        return path.substring(idx + marker.length)
    }
}
