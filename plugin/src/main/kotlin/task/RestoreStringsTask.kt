package task

import components.PrintUtils
import components.restoreResourceFiles
import components.restoreStringRes
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Runs after merge*Resources: restores string resources from backup.
 */
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
