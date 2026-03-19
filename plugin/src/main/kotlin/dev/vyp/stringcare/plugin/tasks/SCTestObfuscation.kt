package dev.vyp.stringcare.plugin.tasks

import com.google.gson.Gson
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.internal.backupResourceFiles
import dev.vyp.stringcare.plugin.internal.extractFingerprint
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.modifyXML
import dev.vyp.stringcare.plugin.internal.parseXML
import dev.vyp.stringcare.plugin.internal.restoreResourceFiles
import dev.vyp.stringcare.plugin.internal.runCommand
import dev.vyp.stringcare.plugin.internal.signingReportTask
import dev.vyp.stringcare.plugin.internal.Stark
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SCTestObfuscation : DefaultTask() {

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
        val gson = Gson()

        val task = this
        val configuration = StringCareConfiguration(module).apply {
            @Suppress("UNCHECKED_CAST")
            val lSrcFolders = gson.fromJson(task.srcFolders, MutableList::class.java)
            if (task.srcFolders.isNotEmpty()) {
                srcFolders.addAll(lSrcFolders as MutableList<String>)
            }
            @Suppress("UNCHECKED_CAST")
            val lStringFiles = gson.fromJson(task.stringFiles, MutableList::class.java)
            if (task.stringFiles.isNotEmpty()) {
                stringFiles.addAll(lStringFiles as MutableList<String>)
            }
            @Suppress("UNCHECKED_CAST")
            val lAssetsFiles = gson.fromJson(task.assetsFiles, MutableList::class.java)
            if (task.assetsFiles.isNotEmpty()) {
                assetsFiles.addAll(lAssetsFiles as MutableList<String>)
            }
            if (task.mockedFingerprint.isNotEmpty()) {
                mockedFingerprint = task.mockedFingerprint
            }
            applicationId = task.applicationId
            skip = task.skip
            debug = task.debug
        }
        println("\t== $variantName ======================================")
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
        val filesToObfuscate = backupResourceFiles(StringCarePlugin.absoluteProjectPath, configuration)
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
        restoreResourceFiles(StringCarePlugin.absoluteProjectPath, module)
        println("== END OBFUSCATION ==================================")
    }
}
