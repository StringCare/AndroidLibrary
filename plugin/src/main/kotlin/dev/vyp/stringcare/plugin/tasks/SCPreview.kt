package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.internal.parseXML
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SCPreview : DefaultTask() {
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
        println("== REPORT ======================================")
        val task = this
        println("\t== $variantName ======================================")
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
        val projectRoot = stringCareService.get().absoluteProjectPath()
        val files = locateResourceFiles(projectRoot, configuration)
        println("\tLocated files(${files.size}) for obfuscating")
        println("\tConfig (module=${configuration.name}, debug=${configuration.debug})")
        files.forEach { file ->
            println("\t- ${file.file.name}")
            println("\t\t${file.module}${File.separator}${file.sourceFolder}${File.separator}")
            val entities = parseXML(file.file)
            println("\tpath: ${file.file.absolutePath}")
            println("")
            println("\t============================")
            entities.forEach { entity ->
                entity.attributes.forEach { attribute ->
                    println("\t\"${attribute.name}\": \"${attribute.value}\"")
                }
                println("\t\"value\": \"${entity.value}\"")
                println("\t============================")
            }
            println("")
            println("\t=== content ================")
            println("" + file.file.getContent())
            println("\t============================")
        }
        println("== END REPORT ==================================")
    }
}
