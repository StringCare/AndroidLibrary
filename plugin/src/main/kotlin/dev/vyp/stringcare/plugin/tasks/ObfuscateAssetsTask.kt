package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.backupAssets
import dev.vyp.stringcare.plugin.internal.backupAssetsFiles
import dev.vyp.stringcare.plugin.internal.fingerPrint
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.locateAssetsFiles
import dev.vyp.stringcare.plugin.internal.obfuscateAssets
import dev.vyp.stringcare.plugin.internal.obfuscateFile
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class ObfuscateAssetsTask : DefaultTask() {
    @get:ServiceReference("stringCareBuildService")
    abstract val stringCareService: Property<StringCareBuildService>

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

    private fun configuration(): StringCareConfiguration =
        StringCareConfiguration(moduleName).apply {
            this.applicationId = this@ObfuscateAssetsTask.applicationId
            this.skip = this@ObfuscateAssetsTask.skip
            this.debug = this@ObfuscateAssetsTask.debug
            this.mockedFingerprint = this@ObfuscateAssetsTask.mockedFingerprint
            srcFolders.addAll(parseJsonStringList(srcFoldersJson))
            stringFiles.addAll(parseJsonStringList(stringFilesJson))
            assetsFiles.addAll(parseJsonStringList(assetsFilesJson))
        }

    @TaskAction
    fun run() {
        val config = configuration()
        Stark.setVerboseNativeLogging(config.debug)
        if (skip) {
            PrintUtils.print(moduleName, "Skipping $variantName")
            return
        }
        if (!Stark.isNativeLibLoaded()) {
            PrintUtils.print(moduleName, "Skipping $variantName (native library not available for this architecture)")
            return
        }
        val tempRoot = stringCareService.get().getOrCreateTempFolder()
        PrintUtils.print("", "ApplicationId: $applicationId", tab = true)
        fingerPrint(moduleName, variantName, config) { key ->
            if (key == "none" || key.trim().isEmpty()) {
                PrintUtils.print("No SHA1 key found for :$moduleName:$variantName")
                return@fingerPrint
            }
            PrintUtils.print(moduleName, "$variantName:$key")
            PrintUtils.print(moduleName, backupAssets)
            backupAssetsFiles(projectPath, config, tempRoot)
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
