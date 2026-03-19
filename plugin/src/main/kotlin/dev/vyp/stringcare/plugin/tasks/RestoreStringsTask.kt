package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.domain.usecases.RestoreFilesUseCase
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.restoreStringRes
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Restores string resources from backups in the source tree")
/**
 * Restores original XML resources after merge resources finished.
 *
 * @property stringCareService Shared build state/provider.
 * @property projectPath Absolute module path used to restore files.
 * @property moduleName Gradle module name.
 * @property skip If true, task exits without side effects.
 */
abstract class RestoreStringsTask : DefaultTask() {
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
        // Restore rewrites sources under projectPath; Gradle has no tracked outputs here. Without this,
        // inputs (projectPath, moduleName) never change between builds so the task can be UP-TO-DATE
        // while sources remain obfuscated from the last ObfuscateStringsTask run.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    /** Restores backed up XML files and resets temp folder state. */
    fun run() {
        if (skip.get()) return
        PrintUtils.print(moduleName.get(), restoreStringRes)
        val service = stringCareService.get()
        val tempRoot = service.getOrCreateTempFolder()
        restoreFilesUseCase.restoreResources(projectPath.get(), moduleName.get(), tempRoot)
        service.resetTempFolder()
    }
}
