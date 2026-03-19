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

/**
 * Prints a debug report of files/entities that StringCare would process.
 *
 * @property stringCareService Shared build state/provider.
 * @property module Target module name.
 * @property assetsFiles JSON array of configured asset patterns.
 * @property stringFiles JSON array of configured XML files.
 * @property srcFolders JSON array of source-set roots.
 * @property debug Enables verbose debug behavior in config.
 * @property skip If true, configuration marks execution as skipped.
 * @property applicationId Android application id for report context.
 * @property mockedFingerprint Optional fixed fingerprint for local testing.
 * @property variantName Variant shown in report headers.
 */
abstract class SCPreview : DefaultTask() {
    @get:ServiceReference("stringCareBuildService")
    abstract val stringCareService: Property<StringCareBuildService>

    @get:Input
    abstract val module: Property<String>

    @get:Input
    abstract val assetsFiles: Property<String>

    @get:Input
    abstract val stringFiles: Property<String>

    @get:Input
    abstract val srcFolders: Property<String>

    @get:Input
    abstract val debug: Property<Boolean>

    @get:Input
    abstract val skip: Property<Boolean>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val mockedFingerprint: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    init {
        assetsFiles.convention("")
        stringFiles.convention("")
        srcFolders.convention("")
        debug.convention(false)
        skip.convention(false)
        applicationId.convention("")
        mockedFingerprint.convention("")
        variantName.convention("debug")
    }

    @TaskAction
    /** Emits report output to stdout with parsed entities and source contents. */
    fun greet() {
        println("== REPORT ======================================")
        val task = this
        println("\t== ${variantName.get()} ======================================")
        val configuration =
            StringCareConfiguration(module.get()).apply {
                srcFolders.addAll(parseJsonStringList(task.srcFolders.get()))
                stringFiles.addAll(parseJsonStringList(task.stringFiles.get()))
                assetsFiles.addAll(parseJsonStringList(task.assetsFiles.get()))
                if (task.mockedFingerprint.get().isNotEmpty()) {
                    mockedFingerprint = task.mockedFingerprint.get()
                }
                applicationId = task.applicationId.get()
                skip = task.skip.get()
                debug = task.debug.get()
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
