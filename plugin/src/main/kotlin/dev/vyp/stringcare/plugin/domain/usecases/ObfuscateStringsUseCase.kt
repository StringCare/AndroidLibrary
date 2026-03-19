package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import dev.vyp.stringcare.plugin.domain.models.forEachWithBackup
import dev.vyp.stringcare.plugin.domain.repositories.ResourceRepository
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.modifyXML
import org.gradle.api.Project

/** Locates string resource files that the plugin may obfuscate. */
class ObfuscateStringsUseCase(
    private val resources: ResourceRepository,
) {
    fun locateTargets(
        projectPath: String,
        configuration: StringCareConfiguration,
    ): List<ResourceFile> = resources.locateResourceFiles(projectPath, configuration)

    fun obfuscate(
        projectPath: String,
        configuration: StringCareConfiguration,
        tempRoot: String,
        key: String,
        project: Project? = null,
    ): Int {
        val files = locateResourceFiles(projectPath, configuration, project)
        forEachWithBackup(files, tempRoot) { resource ->
            modifyXML(resource.file, key, configuration)
        }
        return files.size
    }
}
