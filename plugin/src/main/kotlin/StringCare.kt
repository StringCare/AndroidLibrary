
import components.PrintUtils
import components.config
import components.createExtension
import components.defaultConfig
import components.registerTask
import components.registerVariantObfuscationTasks
import components.absolutePath
import components.tempPath
import org.gradle.api.Plugin
import org.gradle.api.Project

open class StringCare : Plugin<Project> {

    companion object {
        @JvmStatic
        internal lateinit var absoluteProjectPath: String

        private var internalTempDir: String? = null
        @JvmStatic
        var tempFolder: String
            get() = internalTempDir ?: tempPath()
            set(value) {
                internalTempDir = value
            }

        fun resetFolder() {
            internalTempDir = null
        }

        @JvmStatic
        internal var configuration: Configuration = defaultConfig()

        @JvmStatic
        internal var variantMap = mutableMapOf<String, String>()
    }

    private lateinit var project: Project
    private lateinit var extension: Extension

    override fun apply(target: Project) {
        this@StringCare.project = target

        extension = project.createExtension()
        absoluteProjectPath = project.absolutePath()

        project.plugins.withId("com.android.application") {
            // Must register onVariants early; cannot call it from afterEvaluate.
            val config = project.config(extension)
            configuration = config
            project.registerVariantObfuscationTasks(extension, config)
            project.afterEvaluate {
                if (configuration.debug) {
                    PrintUtils.print("PATH", absoluteProjectPath)
                }
                project.registerTask(configuration)
            }
        }

        // Fallback when applied to non-application project: still register preview/test tasks
        if (!project.plugins.hasPlugin("com.android.application")) {
            project.afterEvaluate {
                configuration = project.config(extension)
                project.registerTask(configuration)
            }
        }
    }

    open class Extension {
        var assetsFiles = mutableListOf<String>()
        var stringFiles = mutableListOf<String>()
        var srcFolders = mutableListOf<String>()
        var debug = false
        var skip = false
        var mockedFingerprint = ""
    }

    open class Configuration(var name: String) {
        var assetsFiles = mutableListOf<String>()
        var stringFiles = mutableListOf<String>()
        var srcFolders = mutableListOf<String>()
        var debug = false
        var skip = false
        var applicationId = ""
        var mockedFingerprint = ""
    }
}
