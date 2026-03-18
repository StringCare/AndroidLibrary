package task

import components.PrintUtils
import components.restoreAssetsFiles
import components.restoreAssets
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Runs after merge*Assets: restores asset files from backup.
 */
open class RestoreAssetsTask : DefaultTask() {

    @Input
    var projectPath: String = ""

    @Input
    var moduleName: String = ""

    @Input
    var skip: Boolean = false

    @TaskAction
    fun run() {
        if (skip) return
        PrintUtils.print(moduleName, restoreAssets)
        restoreAssetsFiles(projectPath, moduleName)
    }
}
