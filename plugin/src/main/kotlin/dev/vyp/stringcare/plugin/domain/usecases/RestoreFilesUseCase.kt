package dev.vyp.stringcare.plugin.domain.usecases

import dev.vyp.stringcare.plugin.internal.restoreAssetsFiles
import dev.vyp.stringcare.plugin.internal.restoreResourceFiles
import java.io.File

/** Restores StringCare backups for resources/assets into the source tree. */
class RestoreFilesUseCase {
    fun restoreResources(
        projectPath: String,
        module: String,
        tempRoot: String,
    ): List<File> = restoreResourceFiles(projectPath, module, tempRoot)

    fun restoreAssets(
        projectPath: String,
        module: String,
        tempRoot: String,
    ): List<File> = restoreAssetsFiles(projectPath, module, tempRoot)
}
