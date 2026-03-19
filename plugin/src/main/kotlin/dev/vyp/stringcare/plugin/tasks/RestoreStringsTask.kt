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

abstract class RestoreStringsTask : DefaultTask() {
    @get:ServiceReference("stringCareBuildService")
    abstract val stringCareService: Property<StringCareBuildService>

    @Input
    var projectPath: String = ""

    @Input
    var moduleName: String = ""

    @Input
    var skip: Boolean = false

    @TaskAction
    fun run() {
        if (skip) return
        PrintUtils.print(moduleName, restoreStringRes)
        val service = stringCareService.get()
        val tempRoot = service.getOrCreateTempFolder()
        restoreResourceFiles(projectPath, moduleName, tempRoot)
        service.resetTempFolder()
    }
}
