package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.internal.LoadResult
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

/**
 * Manual verification task that prints before/after XML content for obfuscation.
 *
 * @property stringCareService Shared build state/provider.
 * @property module Target module name.
 * @property assetsFiles JSON array of configured asset patterns.
 * @property stringFiles JSON array of configured XML files.
 * @property srcFolders JSON array of source-set roots.
 * @property debug Enables verbose debug behavior in config.
 * @property skip If true, task exits without side effects.
 * @property applicationId Android application id for native obfuscation.
 * @property mockedFingerprint Optional fixed fingerprint for deterministic runs.
 * @property variantName Variant under test.
 */
abstract class SCTestObfuscation : DefaultTask() {
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
    /** Runs backup, obfuscation and restore while printing full trace to stdout. */
    fun greet() {
        println("== TEST OBFUSCATION ======================================")
        var key = ""
        val task = this
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
        println("\t== ${variantName.get()} ======================================")
        Stark.setVerboseNativeLogging(configuration.debug)
        if (configuration.skip) {
            println("\tSkipping (skip=true)")
            return
        }
        when (val nativeLoad = Stark.isNativeLibLoaded()) {
            LoadResult.Loaded -> Unit
            is LoadResult.Failed -> {
                println("\tSkipping (${nativeLoad.message})")
                return
            }
        }
        signingReportTask().runCommand { _, result ->
            key = result.extractFingerprint(module.get(), variantName.get(), configuration)
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
        restoreResourceFiles(projectRoot, module.get(), tempRoot)
        service.resetTempFolder()
        println("== END OBFUSCATION ==================================")
    }
}
