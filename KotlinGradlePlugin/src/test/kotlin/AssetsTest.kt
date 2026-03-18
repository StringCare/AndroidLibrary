import components.*
import org.junit.Before
import org.junit.Test
import java.io.File

class AssetsTest {

    // private val logger by logger()

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
    fun `01 - (PLUGIN) locate assets files for default configuration`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(locateAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                assetsFiles = mutableListOf("*.json")
            }).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `02 - (PLUGIN) backup assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(backupAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                assetsFiles = mutableListOf("*.json")
            }).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `03 - (PLUGIN) restore assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, report ->
            println(report)
            assert(
                restoreAssetsFiles("$temp${File.separator}$testProjectName", defaultMainModule).isEmpty()
            )
            assert(
                backupAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                }).isNotEmpty()
            )
            assert(
                restoreAssetsFiles("$temp${File.separator}$testProjectName", defaultMainModule).isNotEmpty()
            )
        }
    }

    @Test
    fun `04 - (PLUGIN) asset obfuscation`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint(variant = "prodDebug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files = locateAssetsFiles(
                "$temp${File.separator}$testProjectName",
                configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                })
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    key,
                    it.file,
                    configuration.applicationId
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
            val key = report.extractFingerprint(variant = "prodDebug", configuration = configuration)
            println(key)
            assert(key.isNotEmpty())
            val files = locateAssetsFiles(
                "$temp${File.separator}$testProjectName",
                configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                })
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    key,
                    it.file,
                    configuration.applicationId
                )
                val obfuscated = it.file.getContent()
                println("obfuscated: \n $obfuscated")
                assert(original != obfuscated)
                revealFile(
                    key,
                    it.file,
                    configuration.applicationId
                )
                val reveal = it.file.getContent()
                println("reveal: \n $reveal")
                assert(original == reveal)
            }
        }
    }

}