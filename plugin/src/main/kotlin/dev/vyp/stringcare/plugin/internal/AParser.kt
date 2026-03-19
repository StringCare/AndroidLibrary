package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.AssetsFile
import org.gradle.api.Project
import java.io.File

fun locateAssetsFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
    project: Project? = null,
): List<AssetsFile> =
    locateConfiguredFiles(
        projectPath = projectPath,
        configuration = configuration,
        debugHeader = "== ASSETS FILES FOUND ======================================",
        project = project,
        includeNames = configuration.normalize().assetsFiles,
        isValid = { file, normalized -> file.validForAssetsConfiguration(normalized) },
        mapFile = { file, normalized -> file.assetsFile(normalized) },
    )

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

fun obfuscateFileTo(
    key: String,
    source: File,
    dest: File,
    mockId: String,
) {
    val obfuscation = Stark.obfuscate(key, source.readBytes(), mockId)
    dest.writeBytes(obfuscation)
}

fun revealFile(
    key: String,
    file: File,
    mockId: String = "",
) {
    file.writeBytes(Stark.reveal(key, file.readBytes(), mockId))
}
