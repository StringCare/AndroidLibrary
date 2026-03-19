package dev.vyp.stringcare.plugin.tasks

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.usecases.ObfuscateAssetsUseCase
import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.LoadResult
import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.fingerPrint
import dev.vyp.stringcare.plugin.internal.obfuscateAssets
import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Writes obfuscated assets into a generated directory for AGP to merge. Sources are never modified.
 */
abstract class ObfuscateAssetsTask : DefaultTask() {
    private val obfuscateAssetsUseCase = ObfuscateAssetsUseCase()

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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val assetInputFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

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
        val out = outputDirectory.get().asFile
        if (out.exists()) {
            out.deleteRecursively()
        }
        out.mkdirs()

        val config = configuration()
        Stark.setVerboseNativeLogging(config.debug)
        if (skip.get()) {
            PrintUtils.print(moduleName.get(), "Skipping ${variantName.get()}")
            return
        }
        when (val nativeLoad = Stark.isNativeLibLoaded()) {
            LoadResult.Loaded -> Unit
            is LoadResult.Failed -> {
                PrintUtils.print(moduleName.get(), "Skipping ${variantName.get()} (${nativeLoad.message})")
                return
            }
        }
        PrintUtils.print("", "ApplicationId: ${applicationId.get()}", tab = true)
        fingerPrint(moduleName.get(), variantName.get(), config) { key ->
            if (key == "none" || key.trim().isEmpty()) {
                PrintUtils.print("No SHA1 key found for :${moduleName.get()}:${variantName.get()}")
                return@fingerPrint
            }
            PrintUtils.print(moduleName.get(), "${variantName.get()}:$key")
            obfuscateAssetsUseCase.obfuscate(
                projectPath.get(),
                config,
                out,
                key,
                project,
            )
            PrintUtils.print(moduleName.get(), obfuscateAssets)
        }
    }
}
