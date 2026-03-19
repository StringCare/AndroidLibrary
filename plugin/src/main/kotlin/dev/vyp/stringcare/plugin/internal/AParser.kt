package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.domain.models.AssetsFile
import java.io.File

fun locateAssetsFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
): List<AssetsFile> =
    locateConfiguredFiles(
        projectPath = projectPath,
        configuration = configuration,
        debugHeader = "== ASSETS FILES FOUND ======================================",
        isValid = { file, normalized -> file.validForAssetsConfiguration(normalized) },
        mapFile = { file, normalized -> file.assetsFile(normalized) },
    )

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
    return backupLocatedFiles(files, tempRoot)
}

fun restoreAssetsFiles(
    projectPath: String,
    module: String,
    tempRoot: String,
): List<File> = restoreBackedUpFiles(projectPath, module, tempRoot)

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
