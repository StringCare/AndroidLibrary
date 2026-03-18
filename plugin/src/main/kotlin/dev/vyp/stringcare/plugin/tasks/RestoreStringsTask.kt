package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.restoreResourceFiles
import dev.vyp.stringcare.plugin.internal.restoreStringRes
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class RestoreStringsTask : DefaultTask() {

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
        restoreResourceFiles(projectPath, moduleName)
    }
}
