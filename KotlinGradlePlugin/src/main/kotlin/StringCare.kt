
import components.*
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

        this.project.afterEvaluate {
            this.project.applicationVariants()?.forEach { variant ->
                variantMap[variant.name] = variant.applicationId
            }
            configuration = this.project.config(extension)

            if (configuration.debug) {
                PrintUtils.print("PATH", absoluteProjectPath)
            }
            this.project.registerTask(configuration)
        }
        this.project.gradle.addBuildListener(
            ExecutionListener(
                debug = configuration.debug,
                dataFound = { _, _ ->
                    // nothing to do here
                },
                mergeResourcesStart = { module, variant ->
                    configuration.name = module
                    if (variantMap.containsKey(variant)) {
                        configuration.applicationId = variantMap[variant] ?: ""
                    }
                    PrintUtils.print("", "ApplicationId: ${configuration.applicationId}", tab = true)
                    fingerPrint(module, variant, configuration) { key ->
                        if (configuration.skip) {
                            PrintUtils.print(module, "Skipping $variant")
                            return@fingerPrint
                        }

                        if ("none" == key || key.trim().isEmpty()) {
                            PrintUtils.print("No SHA1 key found for :$module:$variant")
                            return@fingerPrint
                        }

                        PrintUtils.print(module, "$variant:$key")
                        PrintUtils.print(module, backupStringRes)
                        backupResourceFiles(absoluteProjectPath, configuration)


                        val files = locateResourceFiles(absoluteProjectPath, configuration)
                        files.forEach { file ->
                            modifyXML(file.file, key, configuration)
                        }
                    }
                    PrintUtils.print(module, obfuscateStringRes)
                },
                mergeResourcesFinish = { module, _ ->
                    if (configuration.skip) {
                        return@ExecutionListener
                    }
                    PrintUtils.print(module, restoreStringRes)
                    restoreResourceFiles(absoluteProjectPath, module)
                },
                mergeAssetsStart = { module, variant ->
                    configuration.name = module
                    if (variantMap.containsKey(variant)) {
                        configuration.applicationId = variantMap[variant] ?: ""
                    }
                    PrintUtils.print("", "ApplicationId: ${configuration.applicationId}", tab = true)
                    fingerPrint(module, variant, configuration) { key ->
                        if (configuration.skip) {
                            PrintUtils.print(module, "Skipping $variant")
                            return@fingerPrint
                        }

                        if ("none" == key || key.trim().isEmpty()) {
                            PrintUtils.print("No SHA1 key found for :$module:$variant")
                            return@fingerPrint
                        }

                        PrintUtils.print(module, "$variant:$key")
                        PrintUtils.print(module, backupAssets)
                        backupAssetsFiles(absoluteProjectPath, configuration)


                        val files = locateAssetsFiles(absoluteProjectPath, configuration)
                        files.forEach { file ->
                            if (configuration.debug) {
                                PrintUtils.print(null, file.file.getContent())
                            }
                            obfuscateFile(
                                key,
                                file.file,
                                configuration.applicationId
                            )
                            if (configuration.debug) {
                                PrintUtils.print(null, file.file.getContent())
                            }
                        }
                        PrintUtils.print(module, obfuscateAssets)
                    }
                },
                mergeAssetsFinish = { module, _ ->
                    if (configuration.skip) {
                        return@ExecutionListener
                    }
                    PrintUtils.print(module, restoreAssets)
                    restoreAssetsFiles(absoluteProjectPath, module)
                }

            ))
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


