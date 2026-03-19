package dev.vyp.stringcare.plugin.domain.repositories

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.ResourceFile

/** Locates Android string resource files eligible for obfuscation. */
fun interface ResourceRepository {
    fun locateResourceFiles(
        projectPath: String,
        configuration: StringCareConfiguration,
    ): List<ResourceFile>
}
