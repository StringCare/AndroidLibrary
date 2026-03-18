package components

import StringCare
import StringCare.Configuration
import StringCare.Extension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.gson.Gson
import org.gradle.api.Project
import task.ObfuscateAssetsTask
import task.ObfuscateStringsTask
import task.RestoreAssetsTask
import task.RestoreStringsTask

private fun String.variantTaskSuffix(): String = replaceFirstChar { it.uppercase() }

/**
 * Registers per-variant obfuscation tasks and wires them to merge/generate tasks.
 * Replaces the BuildListener (ExecutionListener) approach.
 * Wiring to merge/generate tasks is deferred to afterEvaluate since those tasks are created after onVariants.
 */
fun Project.registerVariantObfuscationTasks(extension: Extension, config: Configuration) {
    val androidComponents = extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
        ?: return

    val gson = Gson()
    val projectPath = StringCare.absoluteProjectPath
    val moduleName = config.name
    val variantNames = mutableListOf<String>()

    androidComponents.onVariants(androidComponents.selector().all()) { variant ->
        val variantName: String = variant.name
        val applicationId = variant.applicationId.get()
        StringCare.variantMap[variantName] = applicationId
        val variantCapitalized = variantName.variantTaskSuffix()

        // Strings: before merge*Resources -> obfuscate; after merge*Resources -> restore
        val beforeStrings = tasks.register(
            "stringcareBeforeMergeResources$variantCapitalized",
            ObfuscateStringsTask::class.java
        ) {
            it.projectPath = projectPath
            it.moduleName = moduleName
            it.variantName = variantName
            it.applicationId = applicationId
            it.skip = config.skip
            it.debug = config.debug
            it.mockedFingerprint = config.mockedFingerprint
            it.srcFoldersJson = gson.toJson(config.srcFolders)
            it.stringFilesJson = gson.toJson(config.stringFiles)
            it.assetsFilesJson = gson.toJson(config.assetsFiles)
        }
        val afterStrings = tasks.register(
            "stringcareAfterMergeResources$variantCapitalized",
            RestoreStringsTask::class.java
        ) {
            it.projectPath = projectPath
            it.moduleName = moduleName
            it.skip = config.skip
        }
        variantNames.add(variantCapitalized)

        // Assets: before generate*Assets -> obfuscate; after merge*Assets -> restore
        val beforeAssets = tasks.register(
            "stringcareBeforeMergeAssets$variantCapitalized",
            ObfuscateAssetsTask::class.java
        ) {
            it.projectPath = projectPath
            it.moduleName = moduleName
            it.variantName = variantName
            it.applicationId = applicationId
            it.skip = config.skip
            it.debug = config.debug
            it.mockedFingerprint = config.mockedFingerprint
            it.srcFoldersJson = gson.toJson(config.srcFolders)
            it.stringFilesJson = gson.toJson(config.stringFiles)
            it.assetsFilesJson = gson.toJson(config.assetsFiles)
        }
        val afterAssets = tasks.register(
            "stringcareAfterMergeAssets$variantCapitalized",
            RestoreAssetsTask::class.java
        ) {
            it.projectPath = projectPath
            it.moduleName = moduleName
            it.skip = config.skip
        }
    }

    project.afterEvaluate {
        for (variantCapitalized in variantNames) {
            val mergeRes = "merge${variantCapitalized}Resources"
            val beforeRes = "stringcareBeforeMergeResources$variantCapitalized"
            val afterRes = "stringcareAfterMergeResources$variantCapitalized"
            val processRes = "process${variantCapitalized}Resources"
            tasks.findByName(mergeRes)?.dependsOn(beforeRes)
            tasks.findByName(afterRes)?.dependsOn(mergeRes)
            tasks.findByName(processRes)?.dependsOn(afterRes)
            val genAssets = "generate${variantCapitalized}Assets"
            val beforeAssets = "stringcareBeforeMergeAssets$variantCapitalized"
            val mergeAssets = "merge${variantCapitalized}Assets"
            val afterAssets = "stringcareAfterMergeAssets$variantCapitalized"
            tasks.findByName(genAssets)?.dependsOn(beforeAssets)
            tasks.findByName(afterAssets)?.dependsOn(mergeAssets)
            tasks.findByName("compress${variantCapitalized}Assets")?.dependsOn(afterAssets)
        }
    }
}
