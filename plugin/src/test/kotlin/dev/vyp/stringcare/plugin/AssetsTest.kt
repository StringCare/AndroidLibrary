package dev.vyp.stringcare.plugin

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
    fun `02 - (PLUGIN) backup assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(
                backupAssetsFiles(
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
    fun `03 - (PLUGIN) restore assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, report ->
            println(report)
            val projectRoot = "$temp${File.separator}$testProjectName"
            val cfg =
                configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                }
            assert(restoreAssetsFiles(projectRoot, defaultMainModule, temp).isEmpty())
            assert(backupAssetsFiles(projectRoot, cfg, temp).isNotEmpty())
            assert(restoreAssetsFiles(projectRoot, defaultMainModule, temp).isNotEmpty())
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
