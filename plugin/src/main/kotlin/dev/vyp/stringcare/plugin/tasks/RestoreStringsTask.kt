package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.restoreResourceFiles
import dev.vyp.stringcare.plugin.internal.restoreStringRes
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Restores string resources from backups in the source tree")
abstract class RestoreStringsTask : DefaultTask() {
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
    fun run() {
        if (skip.get()) return
        PrintUtils.print(moduleName.get(), restoreStringRes)
        val service = stringCareService.get()
        val tempRoot = service.getOrCreateTempFolder()
        restoreResourceFiles(projectPath.get(), moduleName.get(), tempRoot)
        service.resetTempFolder()
    }
}
