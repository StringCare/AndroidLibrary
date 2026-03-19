package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.domain.models.AssetsFile
import java.io.File

fun locateAssetsFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
): List<AssetsFile> {
    if (configuration.debug) {
        println("== ASSETS FILES FOUND ======================================")
    }
    val normalized = configuration.normalize()
    return File(projectPath)
        .walkTopDown()
        .onEnter { dir ->
            val path = dir.path.replace('\\', '/')
            !path.contains("/build/") &&
                !path.contains("/.gradle/") &&
                !path.contains("/.git/") &&
                !path.contains("/node_modules/")
        }.filter { file ->
            !file.isDirectory && file.validForAssetsConfiguration(normalized)
        }.mapNotNull { file ->
            file.assetsFile(normalized)
        }.toList()
}

/** Test convenience: uses [StringCareSession] temp root set by [tempPath]. */
fun backupAssetsFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
): List<AssetsFile> = backupAssetsFiles(projectPath, configuration, StringCareSession.requireTestTempRoot())

fun backupAssetsFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
    tempRoot: String,
): List<AssetsFile> {
    val files = locateAssetsFiles(projectPath, configuration.normalize())
    files.forEach { resource ->
        resource.backup(tempRoot)
    }
    return files
}

fun restoreAssetsFiles(
    projectPath: String,
    module: String,
    tempRoot: String,
): List<File> {
    val resourceFiles =
        File("$tempRoot${File.separator}$module")
            .walkTopDown()
            .toList()
            .filter { file ->
                !file.isDirectory
            }.map { file ->
                file.restore(projectPath, tempRoot)
            }
    return resourceFiles
}

/** Test convenience: uses [StringCareSession] temp root set by [tempPath]. */
fun restoreAssetsFiles(
    projectPath: String,
    module: String,
): List<File> {
    val tempRoot = StringCareSession.requireTestTempRoot()
    val out = restoreAssetsFiles(projectPath, module, tempRoot)
    StringCarePlugin.resetFolder()
    return out
}

fun obfuscateFile(
    key: String,
    file: File,
    mockId: String,
) {
    val obfuscation =
        Stark.obfuscate(
            key,
            file.readBytes(),
            mockId,
        )
    file.writeBytes(obfuscation)
}

fun revealFile(
    key: String,
    file: File,
    mockId: String = "",
) {
    file.writeBytes(Stark.reveal(key, file.readBytes(), mockId))
}
