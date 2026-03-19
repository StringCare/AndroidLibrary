package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.backupResourceFiles
import dev.vyp.stringcare.plugin.internal.extractFingerprint
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.modifyXML
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.internal.parseXML
import dev.vyp.stringcare.plugin.internal.restoreResourceFiles
import dev.vyp.stringcare.plugin.internal.runCommand
import dev.vyp.stringcare.plugin.internal.signingReportTask
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class SCTestObfuscation : DefaultTask() {
    @get:ServiceReference("stringCareBuildService")
    abstract val stringCareService: Property<StringCareBuildService>

    @Input
    var module = String()

    @Input
    var assetsFiles = String()

    @Input
    var stringFiles = String()

    @Input
    var srcFolders = String()

    @Input
    var debug = false

    @Input
    var skip = false

    @Input
    var applicationId = ""

    @Input
    var mockedFingerprint = ""

    @Input
    var variantName = "debug"

    @TaskAction
    fun greet() {
        println("== TEST OBFUSCATION ======================================")
        var key = ""
        val task = this
        val configuration =
            StringCareConfiguration(module).apply {
                srcFolders.addAll(parseJsonStringList(task.srcFolders))
                stringFiles.addAll(parseJsonStringList(task.stringFiles))
                assetsFiles.addAll(parseJsonStringList(task.assetsFiles))
                if (task.mockedFingerprint.isNotEmpty()) {
                    mockedFingerprint = task.mockedFingerprint
                }
                applicationId = task.applicationId
                skip = task.skip
                debug = task.debug
            }
        println("\t== $variantName ======================================")
        Stark.setVerboseNativeLogging(configuration.debug)
        if (configuration.skip) {
            println("\tSkipping (skip=true)")
            return
        }
        if (!Stark.isNativeLibLoaded()) {
            println("\tSkipping (native library not available for this architecture)")
            return
        }
        signingReportTask().runCommand { _, result ->
            key = result.extractFingerprint(module, variantName, configuration)
        }
        val service = stringCareService.get()
        val tempRoot = service.getOrCreateTempFolder()
        val projectRoot = service.absoluteProjectPath()
        val filesToObfuscate = backupResourceFiles(projectRoot, configuration, tempRoot)
        filesToObfuscate.forEach { file ->
            val originalEntities = parseXML(file.file)
            println("\t============================")
            println("\tpath: ${file.file.absolutePath}")
            originalEntities.forEach { entity ->
                entity.attributes.forEach { attribute ->
                    println("\"${attribute.name}\": \"${attribute.value}\"")
                }
                println("\"\tvalue\": \"${entity.value}\"")
                println("\t============================")
            }
            println("")
            println("\t=== content ================")
            println(file.file.getContent())
            println("\t============================")
            modifyXML(file.file, key, configuration)
            println("\t=== content obfuscated ================")
            println(file.file.getContent())
            println("\t============================")
        }
        restoreResourceFiles(projectRoot, module, tempRoot)
        service.resetTempFolder()
        println("== END OBFUSCATION ==================================")
    }
}
