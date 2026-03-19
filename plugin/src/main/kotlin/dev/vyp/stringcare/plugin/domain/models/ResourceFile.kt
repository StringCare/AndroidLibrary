package dev.vyp.stringcare.plugin.domain.models

import java.io.File

data class ResourceFile(
    val file: File,
    val sourceFolder: String,
    val module: String,
) : Backupable {
    override fun backup(tempRoot: String): File {
        val cleanPath =
            "$tempRoot${File.separator}$module${File.separator}$sourceFolder${file.absolutePath.split(sourceFolder)[1]}"
                .replace("${File.separator}${File.separator}", File.separator)
        val backupFile = File(cleanPath)
        copyToBackupAtomically(file, backupFile)
        return backupFile
    }
}
