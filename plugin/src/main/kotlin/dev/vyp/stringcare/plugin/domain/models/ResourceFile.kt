package dev.vyp.stringcare.plugin.domain.models

import java.io.File

/**
 * String XML resource file selected for obfuscation and backup.
 *
 * Example:
 * `ResourceFile(File("src/main/res/values/strings.xml"), "src/main", "app")`
 *
 * @property file Physical file in the project.
 * @property sourceFolder Source-set root used to rebuild relative backup paths.
 * @property module Gradle module name that owns the resource file.
 */
data class ResourceFile(
    val file: File,
    val sourceFolder: String,
    val module: String,
) : Backupable {
    /**
     * Creates a backup copy of this resource file inside [tempRoot], preserving module/source relative path.
     */
    override fun backup(tempRoot: String): File {
        val cleanPath =
            "$tempRoot${File.separator}$module${File.separator}$sourceFolder${file.absolutePath.split(sourceFolder)[1]}"
                .replace("${File.separator}${File.separator}", File.separator)
        val backupFile = File(cleanPath)
        copyToBackupAtomically(file, backupFile)
        return backupFile
    }
}
