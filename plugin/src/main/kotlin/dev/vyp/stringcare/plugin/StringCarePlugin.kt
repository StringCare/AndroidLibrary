package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.PrintUtils
import dev.vyp.stringcare.plugin.internal.StringCareSession
import dev.vyp.stringcare.plugin.internal.absolutePath
import dev.vyp.stringcare.plugin.internal.config
import dev.vyp.stringcare.plugin.internal.registerTask
import dev.vyp.stringcare.plugin.internal.registerVariantObfuscationTasks
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildServiceSpec

/**
 * StringCare Gradle plugin entry point. Applies string/asset obfuscation for Android application projects.
 */
class StringCarePlugin : Plugin<Project> {
    companion object {
        /** Test hook: clears the in-memory temp root used by [dev.vyp.stringcare.plugin.internal.tempPath]. */
        @JvmStatic
        fun resetFolder() {
            StringCareSession.clearTestTempRoot()
        }
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create("stringcare", StringCareExtension::class.java)
        val buildService = registerStringCareService(target)

        target.plugins.withId("com.android.application") {
            val config = target.config(extension)
            buildService.get().setResolvedConfiguration(config)
            target.registerVariantObfuscationTasks(config, buildService)
            target.afterEvaluate {
                val resolved = buildService.get().getResolvedConfiguration()
                if (resolved.debug) {
                    PrintUtils.print("PATH", buildService.get().absoluteProjectPath())
                }
                target.registerTask(resolved, buildService)
            }
        }

        if (!target.plugins.hasPlugin("com.android.application")) {
            target.afterEvaluate {
                val config = target.config(extension)
                buildService.get().setResolvedConfiguration(config)
                target.registerTask(config, buildService)
            }
        }
    }

    private fun registerStringCareService(target: Project): Provider<StringCareBuildService> =
        target.gradle.sharedServices.registerIfAbsent(
            "stringCareBuildService",
            StringCareBuildService::class.java,
            Action<BuildServiceSpec<StringCareBuildService.Params>> { spec ->
                spec.parameters.absoluteProjectPath.set(target.provider { target.absolutePath() })
            },
        )
}

/**
 * Internal resolved configuration (module name, applicationId, etc.).
 */
open class StringCareConfiguration(
    var name: String,
) {
    var assetsFiles = mutableListOf<String>()
    var stringFiles = mutableListOf<String>()
    var srcFolders = mutableListOf<String>()
    var debug = false
    var skip = false
    var applicationId = ""
    var mockedFingerprint = ""
}
