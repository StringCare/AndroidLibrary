package dev.vyp.stringcare.plugin.tasks

import com.google.gson.Gson
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.backupAssets
import dev.vyp.stringcare.plugin.internal.backupAssetsFiles
import dev.vyp.stringcare.plugin.internal.fingerPrint
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.locateAssetsFiles
import dev.vyp.stringcare.plugin.internal.obfuscateAssets
import dev.vyp.stringcare.plugin.internal.obfuscateFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class ObfuscateAssetsTask : DefaultTask() {

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

    private fun configuration(): StringCareConfiguration {
        val gson = Gson()
        return StringCareConfiguration(moduleName).apply {
            this.applicationId = this@ObfuscateAssetsTask.applicationId
            this.skip = this@ObfuscateAssetsTask.skip
            this.debug = this@ObfuscateAssetsTask.debug
            this.mockedFingerprint = this@ObfuscateAssetsTask.mockedFingerprint
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(srcFoldersJson, MutableList::class.java)?.let { srcFolders.addAll(it as List<String>) }
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(stringFilesJson, MutableList::class.java)?.let { stringFiles.addAll(it as List<String>) }
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(assetsFilesJson, MutableList::class.java)?.let { assetsFiles.addAll(it as List<String>) }
        }
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
            PrintUtils.print(moduleName, backupAssets)
            backupAssetsFiles(projectPath, config)
            val files = locateAssetsFiles(projectPath, config)
            files.forEach { file ->
                if (debug) PrintUtils.print(null, file.file.getContent())
                obfuscateFile(key, file.file, config.applicationId)
                if (debug) PrintUtils.print(null, file.file.getContent())
            }
            PrintUtils.print(moduleName, obfuscateAssets)
        }
    }
}
