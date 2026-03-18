package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.absolutePath
import dev.vyp.stringcare.plugin.internal.config
import dev.vyp.stringcare.plugin.internal.defaultConfig
import dev.vyp.stringcare.plugin.internal.registerTask
import dev.vyp.stringcare.plugin.internal.registerVariantObfuscationTasks
import dev.vyp.stringcare.plugin.internal.tempPath
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * StringCare Gradle plugin entry point. Applies string/asset obfuscation for Android application projects.
 */
class StringCarePlugin : Plugin<Project> {

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
        internal var configuration: StringCareConfiguration = defaultConfig()

        @JvmStatic
        internal var variantMap = mutableMapOf<String, String>()
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create("stringcare", StringCareExtension::class.java)
        absoluteProjectPath = target.absolutePath()

        target.plugins.withId("com.android.application") {
            val config = target.config(extension)
            configuration = config
            target.registerVariantObfuscationTasks(extension, config)
            target.afterEvaluate {
                if (configuration.debug) {
                    PrintUtils.print("PATH", absoluteProjectPath)
                }
                target.registerTask(configuration)
            }
        }

        if (!target.plugins.hasPlugin("com.android.application")) {
            target.afterEvaluate {
                configuration = target.config(extension)
                target.registerTask(configuration)
            }
        }
    }
}

/**
 * Internal resolved configuration (module name, applicationId, etc.).
 */
open class StringCareConfiguration(var name: String) {
    var assetsFiles = mutableListOf<String>()
    var stringFiles = mutableListOf<String>()
    var srcFolders = mutableListOf<String>()
    var debug = false
    var skip = false
    var applicationId = ""
    var mockedFingerprint = ""
}
