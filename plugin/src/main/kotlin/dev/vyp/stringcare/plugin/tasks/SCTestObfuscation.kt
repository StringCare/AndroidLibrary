package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.usecases.ObfuscateStringsUseCase
import dev.vyp.stringcare.plugin.infrastructure.gradle.FileSystemResourceRepository
import dev.vyp.stringcare.plugin.internal.LoadResult
import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.extractFingerprint
import dev.vyp.stringcare.plugin.internal.getContent
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.internal.parseXML
import dev.vyp.stringcare.plugin.internal.runCommand
import dev.vyp.stringcare.plugin.internal.signingReportTask
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files

/**
 * Manual verification task that prints before/after XML content for obfuscation into a temp directory
 * (sources are never modified).
 */
abstract class SCTestObfuscation : DefaultTask() {
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
        val projectRoot = project.projectDir.absolutePath
        val filesToObfuscate = locateResourceFiles(projectRoot, configuration, project)
        val outRoot = Files.createTempDirectory("stringcare-test-obf").toFile()
        try {
            val useCase = ObfuscateStringsUseCase(FileSystemResourceRepository())
            useCase.obfuscate(projectRoot, configuration, outRoot, key, project)
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
                val outFile = File(outRoot, file.relativePathUnderRes())
                println("\t=== content obfuscated (generated) ================")
                println(outFile.getContent())
                println("\t============================")
            }
        } finally {
            outRoot.deleteRecursively()
        }
        println("== END OBFUSCATION ==================================")
    }
}
