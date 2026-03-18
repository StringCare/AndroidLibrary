import components.*
import models.StringEntity
import org.junit.Before
import org.junit.Test
import utils.modifyForTest
import java.io.File

class SCTest {

    private val configuration = defaultConfig().apply {
        debug = true
        applicationId = "com.stringcare.sample"
        stringFiles.add("strings_extra.xml")
        srcFolders.add("src/other_source")
    }

    @Before
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
        StringCare.resetFolder()
    }

    @Test
    fun `05 - (PLUGIN) backup string files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(backupResourceFiles("$temp${File.separator}$testProjectName", configuration).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `06 - (PLUGIN) restore string files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, report ->
            println(report)
            assert(
                restoreResourceFiles("$temp${File.separator}$testProjectName", defaultMainModule).isEmpty()
            )
            assert(
                backupResourceFiles("$temp${File.separator}$testProjectName", configuration).isNotEmpty()
            )
            assert(
                restoreResourceFiles("$temp${File.separator}$testProjectName", defaultMainModule).isNotEmpty()
            )
        }
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
                    val obfuscated = obfuscateStringEntity(
                        key,
                        entity,
                        configuration.applicationId
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
                    val obfuscated = obfuscateStringEntity(
                        key,
                        entity,
                        configuration.applicationId
                    )
                    assert(obfuscated.value != entity.value)

                    val original = revealStringEntity(
                        key,
                        obfuscated,
                        configuration.applicationId
                    )

                    assert(
                        original.value == when (entity.androidTreatment) {
                            true -> entity.value.androidTreatment()
                            else -> entity.value
                        }
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
    fun `11 - (PLUGIN) obfuscate, restore and compare xml values with originals`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val files = backupResourceFiles("$temp${File.separator}$testProjectName", configuration)
            assert(files.isNotEmpty())
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

            val restoredFiles = restoreResourceFiles("$temp${File.separator}$testProjectName", defaultMainModule)
            assert(restoredFiles.isNotEmpty())

            val originalEntities = mutableListOf<StringEntity>()
            files.forEach { file ->
                originalEntities.addAll(parseXML(file.file))
            }
            assert(originalEntities.isNotEmpty())

            val restoredEntities = mutableListOf<StringEntity>()
            restoredFiles.forEach { file ->
                restoredEntities.addAll(parseXML(file))
            }
            assert(restoredEntities.isNotEmpty())

            originalEntities.forEach { entity ->
                val eq = restoredEntities.find {
                    it.name == entity.name
                }
                eq?.let {
                    assert(entity.name == it.name)
                    assert(entity.value == it.value)
                }
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
                    configuration
                )
            }
            val filesObfuscated = locateResourceFiles(
                "$temp${File.separator}$testProjectName",
                configuration
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