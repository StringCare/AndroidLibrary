package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.locateAssetsFiles
import dev.vyp.stringcare.plugin.internal.obfuscateFileTo
import org.gradle.api.Project
import java.io.File

/** Obfuscates configured asset files into a generated output directory (does not modify sources). */
class ObfuscateAssetsUseCase {
    fun obfuscate(
        projectPath: String,
        configuration: StringCareConfiguration,
        outputAssetsRoot: File,
        key: String,
        project: Project? = null,
    ): Int {
        val files = locateAssetsFiles(projectPath, configuration, project)
        files.forEach { asset ->
            val dest = File(outputAssetsRoot, asset.relativePathUnderAssets())
            dest.parentFile?.mkdirs()
            obfuscateFileTo(key, asset.file, dest, configuration.applicationId)
        }
        return files.size
    }
}
