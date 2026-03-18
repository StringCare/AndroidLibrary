package task

import StringCare
import StringCare.Configuration
import com.google.gson.Gson
import components.PrintUtils
import components.backupResourceFiles
import components.fingerPrint
import components.locateResourceFiles
import components.modifyXML
import components.obfuscateStringRes
import components.backupStringRes
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Runs before merge*Resources: backs up and obfuscates string resources for one variant.
 */
open class ObfuscateStringsTask : DefaultTask() {

    @Input
    var projectPath: String = ""

    @Input
    var moduleName: String = ""

    @Input
    var variantName: String = ""

    @Input
    var applicationId: String = ""

    @Input
    var skip: Boolean = false

    @Input
    var debug: Boolean = false

    @Input
    var mockedFingerprint: String = ""

    @Input
    var srcFoldersJson: String = "[]"

    @Input
    var stringFilesJson: String = "[]"

    @Input
    var assetsFilesJson: String = "[]"

    private fun configuration(): Configuration {
        val gson = Gson()
        val config = Configuration(moduleName).apply {
            this.applicationId = this@ObfuscateStringsTask.applicationId
            this.skip = this@ObfuscateStringsTask.skip
            this.debug = this@ObfuscateStringsTask.debug
            this.mockedFingerprint = this@ObfuscateStringsTask.mockedFingerprint
            gson.fromJson(srcFoldersJson, MutableList::class.java)?.let { srcFolders.addAll(it as List<String>) }
            gson.fromJson(stringFilesJson, MutableList::class.java)?.let { stringFiles.addAll(it as List<String>) }
            gson.fromJson(assetsFilesJson, MutableList::class.java)?.let { assetsFiles.addAll(it as List<String>) }
        }
        return config
    }

    @TaskAction
    fun run() {
        val config = configuration()
        if (skip) {
            PrintUtils.print(moduleName, "Skipping $variantName")
            return
        }
        PrintUtils.print("", "ApplicationId: $applicationId", tab = true)
        fingerPrint(moduleName, variantName, config) { key ->
            if (key == "none" || key.trim().isEmpty()) {
                PrintUtils.print("No SHA1 key found for :$moduleName:$variantName")
                return@fingerPrint
            }
            PrintUtils.print(moduleName, "$variantName:$key")
            PrintUtils.print(moduleName, backupStringRes)
            backupResourceFiles(projectPath, config)
            val files = locateResourceFiles(projectPath, config)
            files.forEach { modifyXML(it.file, key, config) }
        }
        PrintUtils.print(moduleName, obfuscateStringRes)
    }
}
