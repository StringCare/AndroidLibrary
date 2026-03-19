package dev.vyp.stringcare.plugin.infrastructure.filesystem

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.AssetsFile
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import dev.vyp.stringcare.plugin.internal.backupAssetsFiles
import dev.vyp.stringcare.plugin.internal.backupResourceFiles

/** Coordinates backup of string and asset trees into the StringCare temp root. */
class BackupService {
    fun backupStringResources(
        projectPath: String,
        configuration: StringCareConfiguration,
        tempRoot: String,
    ): List<ResourceFile> = backupResourceFiles(projectPath, configuration, tempRoot)

    fun backupAssetFiles(
        projectPath: String,
        configuration: StringCareConfiguration,
        tempRoot: String,
    ): List<AssetsFile> = backupAssetsFiles(projectPath, configuration, tempRoot)
}
