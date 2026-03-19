package dev.vyp.stringcare.plugin.infrastructure.gradle

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import dev.vyp.stringcare.plugin.domain.repositories.ResourceRepository
import dev.vyp.stringcare.plugin.internal.locateResourceFiles as internalLocate

/** Default [ResourceRepository] backed by filesystem scanning. */
class FileSystemResourceRepository : ResourceRepository {
    override fun locateResourceFiles(
        projectPath: String,
        configuration: StringCareConfiguration,
    ): List<ResourceFile> = internalLocate(projectPath, configuration)
}
