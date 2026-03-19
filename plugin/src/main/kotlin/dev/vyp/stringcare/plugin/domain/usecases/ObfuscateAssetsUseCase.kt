package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.forEachWithBackup
import dev.vyp.stringcare.plugin.internal.locateAssetsFiles
import dev.vyp.stringcare.plugin.internal.obfuscateFile
import org.gradle.api.Project

/** Backs up and obfuscates configured asset files. */
class ObfuscateAssetsUseCase {
    fun obfuscate(
        projectPath: String,
        configuration: StringCareConfiguration,
        tempRoot: String,
        key: String,
        project: Project? = null,
    ): Int {
        val files = locateAssetsFiles(projectPath, configuration, project)
        forEachWithBackup(files, tempRoot) { asset ->
            obfuscateFile(key, asset.file, configuration.applicationId)
        }
        return files.size
    }
}
