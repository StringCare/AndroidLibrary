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

    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val moduleName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val skip: Property<Boolean>

    @get:Input
    abstract val debug: Property<Boolean>

    @get:Input
    abstract val mockedFingerprint: Property<String>

    @get:Input
    abstract val srcFoldersJson: Property<String>

    @get:Input
    abstract val stringFilesJson: Property<String>

    @get:Input
    abstract val assetsFilesJson: Property<String>

    init {
        skip.convention(false)
        debug.convention(false)
        mockedFingerprint.convention("")
        srcFoldersJson.convention("[]")
        stringFilesJson.convention("[]")
        assetsFilesJson.convention("[]")
    }

    private fun configuration(): StringCareConfiguration {
        val task = this
        return StringCareConfiguration(task.moduleName.get()).apply {
            applicationId = task.applicationId.get()
            skip = task.skip.get()
            debug = task.debug.get()
            mockedFingerprint = task.mockedFingerprint.get()
            srcFolders.addAll(parseJsonStringList(task.srcFoldersJson.get()))
            stringFiles.addAll(parseJsonStringList(task.stringFilesJson.get()))
            assetsFiles.addAll(parseJsonStringList(task.assetsFilesJson.get()))
        }
    }

    @TaskAction
    fun run() {
        val config = configuration()
        Stark.setVerboseNativeLogging(config.debug)
        if (skip.get()) {
            PrintUtils.print(moduleName.get(), "Skipping ${variantName.get()}")
            return
        }
        if (!Stark.isNativeLibLoaded()) {
            PrintUtils.print(moduleName.get(), "Skipping ${variantName.get()} (native library not available for this architecture)")
            return
        }
        val tempRoot = stringCareService.get().getOrCreateTempFolder()
        PrintUtils.print("", "ApplicationId: ${applicationId.get()}", tab = true)
        fingerPrint(moduleName.get(), variantName.get(), config) { key ->
            if (key == "none" || key.trim().isEmpty()) {
                PrintUtils.print("No SHA1 key found for :${moduleName.get()}:${variantName.get()}")
                return@fingerPrint
            }
            PrintUtils.print(moduleName.get(), "${variantName.get()}:$key")
            PrintUtils.print(moduleName.get(), backupStringRes)
            backupResourceFiles(projectPath.get(), config, tempRoot)
            val files = locateResourceFiles(projectPath.get(), config)
            files.forEach { modifyXML(it.file, key, config) }
        }
        PrintUtils.print(moduleName.get(), obfuscateStringRes)
    }
}
