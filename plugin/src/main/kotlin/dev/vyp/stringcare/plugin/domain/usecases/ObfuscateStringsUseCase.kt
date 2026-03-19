package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.repositories.ResourceRepository
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.modifyXML
import org.gradle.api.Project
import java.io.File

/** Locates string resource files that the plugin may obfuscate. */
class ObfuscateStringsUseCase(
    private val resources: ResourceRepository,
) {
    fun locateTargets(
        projectPath: String,
        configuration: StringCareConfiguration,
    ) = resources.locateResourceFiles(projectPath, configuration)

    fun obfuscate(
        projectPath: String,
        configuration: StringCareConfiguration,
        outputResRoot: File,
        key: String,
        project: Project? = null,
    ): Int {
        val files = locateResourceFiles(projectPath, configuration, project)
        files.forEach { resource ->
            val dest = File(outputResRoot, resource.relativePathUnderRes())
            dest.parentFile?.mkdirs()
            modifyXML(resource.file, dest, key, configuration)
        }
        return files.size
    }
}
