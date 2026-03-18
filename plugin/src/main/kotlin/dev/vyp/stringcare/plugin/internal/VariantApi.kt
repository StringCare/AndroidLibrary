package dev.vyp.stringcare.plugin.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCareExtension
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.tasks.ObfuscateAssetsTask
import dev.vyp.stringcare.plugin.tasks.ObfuscateStringsTask
import dev.vyp.stringcare.plugin.tasks.RestoreAssetsTask
import dev.vyp.stringcare.plugin.tasks.RestoreStringsTask
import com.google.gson.Gson
import org.gradle.api.Project

private fun String.variantTaskSuffix(): String = replaceFirstChar { it.uppercase() }

fun Project.registerVariantObfuscationTasks(extension: StringCareExtension, config: StringCareConfiguration) {
    val androidComponents = extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
        ?: return

    val gson = Gson()
    val projectPath = StringCarePlugin.absoluteProjectPath
    val moduleName = config.name
    val variantNames = mutableListOf<String>()

    androidComponents.onVariants(androidComponents.selector().all()) { variant ->
        val variantName: String = variant.name
        val applicationId = variant.applicationId.get()
        StringCarePlugin.variantMap[variantName] = applicationId
        if (config.applicationId.isEmpty()) config.applicationId = applicationId
        val variantCapitalized = variantName.variantTaskSuffix()

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
        tasks.register(
            "stringcareAfterMergeResources$variantCapitalized",
            RestoreStringsTask::class.java
        ) {
            it.projectPath = projectPath
            it.moduleName = moduleName
            it.skip = config.skip
        }
        variantNames.add(variantCapitalized)

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
        tasks.register(
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
