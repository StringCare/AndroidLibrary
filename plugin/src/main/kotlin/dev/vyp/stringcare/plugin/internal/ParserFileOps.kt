package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.Backupable
import java.io.File

private fun shouldTraverse(dir: File): Boolean {
    val path = dir.path.replace('\\', '/')
    return !path.contains("/build/") &&
        !path.contains("/.gradle/") &&
        !path.contains("/.git/") &&
        !path.contains("/node_modules/")
}

internal fun <T> locateConfiguredFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
    debugHeader: String,
    isValid: (File, StringCareConfiguration) -> Boolean,
    mapFile: (File, StringCareConfiguration) -> T?,
): List<T> {
    if (configuration.debug) {
        println(debugHeader)
    }
    val normalized = configuration.normalize()
    return File(projectPath)
        .walkTopDown()
        .onEnter(::shouldTraverse)
        .filter { file -> !file.isDirectory && isValid(file, normalized) }
        .mapNotNull { file -> mapFile(file, normalized) }
        .toList()
}

internal fun <T : Backupable> backupLocatedFiles(
    files: List<T>,
    tempRoot: String,
): List<T> {
    files.forEach { file -> file.backup(tempRoot) }
    return files
}

internal fun restoreBackedUpFiles(
    projectPath: String,
    module: String,
    tempRoot: String,
): List<File> =
    File("$tempRoot${File.separator}$module")
        .walkTopDown()
        .toList()
        .filter { file -> !file.isDirectory }
        .map { file -> file.restore(projectPath, tempRoot) }
