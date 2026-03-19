package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.Backupable
import org.gradle.api.Project
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
    project: Project? = null,
    includeNames: List<String>,
    isValid: (File, StringCareConfiguration) -> Boolean,
    mapFile: (File, StringCareConfiguration) -> T?,
): List<T> {
    if (configuration.debug) {
        println(debugHeader)
    }
    val normalized = configuration.normalize()
    val candidateFiles =
        when (project) {
            null ->
                File(projectPath)
                    .walkTopDown()
                    .onEnter(::shouldTraverse)
                    .toList()
            else ->
                project
                    .fileTree(projectPath) { tree ->
                        normalized.srcFolders.ifEmpty { listOf("src/main") }.forEach { src ->
                            includeNames.forEach { name ->
                                tree.include("$src/**/$name")
                            }
                        }
                        tree.exclude("**/build/**", "**/.gradle/**", "**/.git/**", "**/node_modules/**")
                    }.files
                    .toList()
        }
    return candidateFiles
        .filter { file -> !file.isDirectory && isValid(file, normalized) }
        .mapNotNull { file -> mapFile(file, normalized) }
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
        .map { file -> file.restore(projectPath, tempRoot, module) }
