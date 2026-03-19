package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.domain.usecases.RestoreFilesUseCase
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.restoreAssets
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Restores asset files from backups in the source tree")
/**
 * Restores original assets after merge assets finished.
 *
 * @property stringCareService Shared build state/provider.
 * @property projectPath Absolute module path used to restore files.
 * @property moduleName Gradle module name.
 * @property skip If true, task exits without side effects.
 */
abstract class RestoreAssetsTask : DefaultTask() {
    private val restoreFilesUseCase = RestoreFilesUseCase()

    @get:ServiceReference("stringCareBuildService")
    abstract val stringCareService: Property<StringCareBuildService>

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val moduleName: Property<String>

    @get:Input
    abstract val skip: Property<Boolean>

    init {
        skip.convention(false)
    }

    @TaskAction
    /** Restores backed up asset files and resets temp folder state. */
    fun run() {
        if (skip.get()) return
        PrintUtils.print(moduleName.get(), restoreAssets)
        val service = stringCareService.get()
        val tempRoot = service.getOrCreateTempFolder()
        restoreFilesUseCase.restoreAssets(projectPath.get(), moduleName.get(), tempRoot)
        service.resetTempFolder()
    }
}
