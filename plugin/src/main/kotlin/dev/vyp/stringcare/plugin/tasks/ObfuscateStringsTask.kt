package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.backupResourceFiles
import dev.vyp.stringcare.plugin.internal.backupStringRes
import dev.vyp.stringcare.plugin.internal.fingerPrint
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.modifyXML
import dev.vyp.stringcare.plugin.internal.obfuscateStringRes
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class ObfuscateStringsTask : DefaultTask() {
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
            this.applicationId = this@ObfuscateStringsTask.applicationId
            this.skip = this@ObfuscateStringsTask.skip
            this.debug = this@ObfuscateStringsTask.debug
            this.mockedFingerprint = this@ObfuscateStringsTask.mockedFingerprint
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
            PrintUtils.print(moduleName, backupStringRes)
            backupResourceFiles(projectPath, config, tempRoot)
            val files = locateResourceFiles(projectPath, config)
            files.forEach { modifyXML(it.file, key, config) }
        }
        PrintUtils.print(moduleName, obfuscateStringRes)
    }
}
