package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.domain.usecases.ObfuscateAssetsUseCase
import dev.vyp.stringcare.plugin.internal.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("integration")
class AssetsTest {
    // private val logger by logger()

    private val configuration =
        defaultConfig().apply {
            debug = true
            applicationId = "com.stringcare.sample"
            stringFiles.add("strings_extra.xml")
            srcFolders.add("src/other_source")
        }

    @BeforeEach
    fun setup() {
        librarySetupTask.runCommand()
    }

    @Test
    fun `01 - (PLUGIN) locate assets files for default configuration`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(
                locateAssetsFiles(
                    "$temp${File.separator}$testProjectName",
                    configuration.apply {
                        assetsFiles = mutableListOf("*.json")
                    },
                ).isNotEmpty(),
            )
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `02 - (PLUGIN) obfuscate assets to output directory`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            val projectRoot = "$temp${File.separator}$testProjectName"
            val outDir = File(projectRoot, "build/stringcare-test-out-assets").apply { mkdirs() }
            val cfg =
                StringCareConfiguration(configuration.name).apply {
                    applicationId = configuration.applicationId
                    debug = configuration.debug
                    srcFolders.addAll(configuration.srcFolders)
                    stringFiles.addAll(configuration.stringFiles)
                    assetsFiles.addAll(listOf("*.json"))
                    mockedFingerprint = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
                }
            val count =
                ObfuscateAssetsUseCase().obfuscate(
                    projectRoot,
                    cfg,
                    outDir,
                    cfg.mockedFingerprint,
                    null,
                )
            assert(count > 0)
            assert(outDir.walkTopDown().any { it.isFile })
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `03 - (PLUGIN) asset obfuscation does not modify source files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            val projectRoot = "$temp${File.separator}$testProjectName"
            val cfg =
                StringCareConfiguration(configuration.name).apply {
                    applicationId = configuration.applicationId
                    debug = configuration.debug
                    srcFolders.addAll(configuration.srcFolders)
                    stringFiles.addAll(configuration.stringFiles)
                    assetsFiles.addAll(listOf("*.json"))
                    mockedFingerprint = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
                }
            val files = locateAssetsFiles(projectRoot, cfg)
            assert(files.isNotEmpty())
            val before = files.associate { it.file.absolutePath to it.file.readBytes() }
            val outDir = File(projectRoot, "build/stringcare-test-out-assets-2").apply { mkdirs() }
            ObfuscateAssetsUseCase().obfuscate(projectRoot, cfg, outDir, cfg.mockedFingerprint, null)
            before.forEach { (path, bytes) ->
                assert(bytes.contentEquals(File(path).readBytes())) { "Source was modified at $path" }
            }
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `04 - (PLUGIN) asset obfuscation`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint(variant = "debug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files =
                locateAssetsFiles(
                    "$temp${File.separator}$testProjectName",
                    configuration.apply {
                        assetsFiles = mutableListOf("*.json")
                    },
                )
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    key,
                    it.file,
                    configuration.applicationId,
                )
                val obfuscated = it.file.getContent()
                println("obfuscated: \n $obfuscated")
                assert(original != obfuscated)
            }
        }
    }

    @Test
    fun `05 - (PLUGIN) asset reveal`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint(variant = "debug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files =
                locateAssetsFiles(
                    "$temp${File.separator}$testProjectName",
                    configuration.apply {
                        assetsFiles = mutableListOf("*.json")
                    },
                )
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    key,
                    it.file,
                    configuration.applicationId,
                )
                val obfuscated = it.file.getContent()
                println("obfuscated: \n $obfuscated")
                assert(original != obfuscated)
                revealFile(
                    key,
                    it.file,
                    configuration.applicationId,
                )
                val reveal = it.file.getContent()
                println("reveal: \n $reveal")
                assert(original == reveal)
            }
        }
    }
}
