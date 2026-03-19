package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import dev.vyp.stringcare.plugin.domain.repositories.ResourceRepository

/** Locates string resource files that the plugin may obfuscate. */
class ObfuscateStringsUseCase(
    private val resources: ResourceRepository,
) {
    fun locateTargets(
        projectPath: String,
        configuration: StringCareConfiguration,
    ): List<ResourceFile> = resources.locateResourceFiles(projectPath, configuration)
}
