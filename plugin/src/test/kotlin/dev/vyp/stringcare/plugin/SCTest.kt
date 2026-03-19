package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.domain.usecases.ObfuscateStringsUseCase
import dev.vyp.stringcare.plugin.infrastructure.gradle.FileSystemResourceRepository
import dev.vyp.stringcare.plugin.internal.*
import dev.vyp.stringcare.plugin.models.StringEntity
import dev.vyp.stringcare.plugin.utils.modifyForTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("integration")
class SCTest {
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
    fun `01 - (PLUGIN) terminal verification`() {
        "echo $extensionName".runCommand { command, result ->
            println(result)
            assert(command.contains(result.normalize()))
        }
    }

    @Test
    fun `02 - (PLUGIN) gradlew signingReport`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `03 - (PLUGIN) fingerprint extraction`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            assert(report.extractFingerprint(variant = "prodDebug", configuration = configuration).split(":").size == 20)
        }
    }

    @Test
    fun `04 - (PLUGIN) locate string files for default configuration`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(locateResourceFiles("$temp${File.separator}$testProjectName", configuration).isNotEmpty())
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `05 - (PLUGIN) obfuscate string files to output directory`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            val projectRoot = "$temp${File.separator}$testProjectName"
            val outDir = File(projectRoot, "build/stringcare-test-out-res").apply { mkdirs() }
            val cfg =
                StringCareConfiguration(configuration.name).apply {
                    applicationId = configuration.applicationId
                    debug = configuration.debug
                    srcFolders.addAll(configuration.srcFolders)
                    stringFiles.addAll(configuration.stringFiles)
                    assetsFiles.addAll(configuration.assetsFiles)
                    mockedFingerprint = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
                }
            val count =
                ObfuscateStringsUseCase(FileSystemResourceRepository()).obfuscate(
                    projectRoot,
                    cfg,
                    outDir,
                    cfg.mockedFingerprint,
                    null,
                )
            assert(count > 0)
            assert(outDir.walkTopDown().any { it.isFile && it.extension == "xml" })
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `06 - (PLUGIN) obfuscate does not modify source files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            val projectRoot = "$temp${File.separator}$testProjectName"
            val files = locateResourceFiles(projectRoot, configuration)
            val before = files.associate { it.file.absolutePath to it.file.readText() }
            val outDir = File(projectRoot, "build/stringcare-test-out-res-2").apply { mkdirs() }
            val cfg =
                StringCareConfiguration(configuration.name).apply {
                    applicationId = configuration.applicationId
                    debug = configuration.debug
                    srcFolders.addAll(configuration.srcFolders)
                    stringFiles.addAll(configuration.stringFiles)
                    assetsFiles.addAll(configuration.assetsFiles)
                    mockedFingerprint = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
                }
            ObfuscateStringsUseCase(FileSystemResourceRepository()).obfuscate(
                projectRoot,
                cfg,
                outDir,
                cfg.mockedFingerprint,
                null,
            )
            before.forEach { (path, text) ->
                assert(text == File(path).readText()) { "Source was modified at $path" }
            }
        }
        StringCarePlugin.resetFolder()
    }

    @Test
    fun `07 - (PLUGIN) xml parsing`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, report ->
            println(report)
            val files = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            files.forEach {
                assert(parseXML(it.file).isNotEmpty())
            }
        }
    }

    @Test
    fun `08 - (PLUGIN) obfuscate string values`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint(variant = "prodDebug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            files.forEach { file ->
                val entities = parseXML(file.file)
                entities.forEach { entity ->
                    val obfuscated =
                        obfuscateStringEntity(
                            key,
                            entity,
                            configuration.applicationId,
                        )
                    assert(obfuscated.value != entity.value)
                }
            }
        }
    }

    @Test
    fun `09 - (PLUGIN) obfuscate and reveal string values`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint(variant = "prodDebug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            files.forEach { file ->
                val entities = parseXML(file.file)
                entities.forEach { entity ->
                    val obfuscated =
                        obfuscateStringEntity(
                            key,
                            entity,
                            configuration.applicationId,
                        )
                    assert(obfuscated.value != entity.value)

                    val original =
                        revealStringEntity(
                            key,
                            obfuscated,
                            configuration.applicationId,
                        )

                    assert(
                        original.value ==
                            when (entity.androidTreatment) {
                                true -> entity.value.androidTreatment()
                                else -> entity.value
                            },
                    )
                }
            }
        }
    }

    @Test
    fun `10 - (PLUGIN) obfuscate xml`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val files = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            files.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isNotEmpty())
                modifyXML(file.file, report.extractFingerprint(configuration = configuration), configuration)
            }
            val filesObfuscated = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `11 - (PLUGIN) obfuscate to output differs from source while sources stay intact`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val projectRoot = "$temp${File.separator}$testProjectName"
            val files = locateResourceFiles(projectRoot, configuration)
            assert(files.isNotEmpty())
            val before = files.associate { it.file.absolutePath to it.file.readText() }
            val outDir = File(projectRoot, "build/stringcare-test-out-res-11").apply { mkdirs() }
            val key = report.extractFingerprint(configuration = configuration)
            ObfuscateStringsUseCase(FileSystemResourceRepository()).obfuscate(
                projectRoot,
                configuration,
                outDir,
                key,
                null,
            )
            before.forEach { (path, text) ->
                assert(text == File(path).readText()) { "Source must stay unchanged at $path" }
            }
            files.forEach { rf ->
                val outFile = File(outDir, rf.relativePathUnderRes())
                assert(outFile.exists())
                assert(outFile.readText() != File(rf.file.absolutePath).readText())
            }
        }
    }

    @Test
    fun `12 - (ANDROID COMPILATION) obfuscate xml and build (not real workflow)`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val files = locateResourceFiles("$temp${File.separator}$testProjectName", configuration)
            files.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isNotEmpty())
                modifyXML(
                    file.file,
                    report.extractFingerprint(configuration = configuration),
                    configuration,
                )
            }
            val filesObfuscated =
                locateResourceFiles(
                    "$temp${File.separator}$testProjectName",
                    configuration,
                )
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `13 - (PLUGIN COMPILATION) plugin with no test`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
            println(report)
        }
    }

    @Test
    fun `14 - (ANDROID COMPILATION) plugin running on Android`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            buildTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("BUILD SUCCESSFUL"))
                println(androidReport)
            }
        }
    }

    @Test
    fun `15 - (GRADLE TASK) stringcarePreview`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            basicGradleTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("END REPORT"))
                println(androidReport)
            }
        }
    }

    @Test
    fun `16 - (GRADLE TASK) stringcareTestObfuscate`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            obfuscationTestGradleTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("END OBFUSCATION"))
                println(androidReport)
            }
        }
    }
}
