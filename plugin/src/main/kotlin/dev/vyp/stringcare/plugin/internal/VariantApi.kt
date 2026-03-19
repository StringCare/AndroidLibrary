package dev.vyp.stringcare.plugin.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import dev.vyp.stringcare.plugin.tasks.ObfuscateAssetsTask
import dev.vyp.stringcare.plugin.tasks.ObfuscateStringsTask
import dev.vyp.stringcare.plugin.tasks.RestoreAssetsTask
import dev.vyp.stringcare.plugin.tasks.RestoreStringsTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider

private fun String.variantTaskSuffix(): String = replaceFirstChar { it.uppercase() }

fun Project.registerVariantObfuscationTasks(
    config: StringCareConfiguration,
    buildService: Provider<StringCareBuildService>,
) {
    val androidComponents =
        extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
            ?: return

    val absoluteProjectPath = buildService.get().absoluteProjectPath()
    val moduleNameValue = config.name
    val srcJson = toJsonStringList(config.srcFolders)
    val strJson = toJsonStringList(config.stringFiles)
    val assetsJson = toJsonStringList(config.assetsFiles)
    val variantNames = mutableListOf<String>()

    androidComponents.onVariants(androidComponents.selector().all()) { variant ->
        val variantName: String = variant.name
        val applicationId = variant.applicationId.get()
        buildService.get().putVariantApplicationId(variantName, applicationId)
        if (config.applicationId.isEmpty()) config.applicationId = applicationId
        val variantCapitalized = variantName.variantTaskSuffix()

        tasks.register(
            "stringcareBeforeMergeResources$variantCapitalized",
            ObfuscateStringsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.variantName.set(variantName)
            t.applicationId.set(applicationId)
            t.skip.set(config.skip)
            t.debug.set(config.debug)
            t.mockedFingerprint.set(config.mockedFingerprint)
            t.srcFoldersJson.set(srcJson)
            t.stringFilesJson.set(strJson)
            t.assetsFilesJson.set(assetsJson)
        }
        tasks.register(
            "stringcareAfterMergeResources$variantCapitalized",
            RestoreStringsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.skip.set(config.skip)
        }
        variantNames.add(variantCapitalized)

        tasks.register(
            "stringcareBeforeMergeAssets$variantCapitalized",
            ObfuscateAssetsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.variantName.set(variantName)
            t.applicationId.set(applicationId)
            t.skip.set(config.skip)
            t.debug.set(config.debug)
            t.mockedFingerprint.set(config.mockedFingerprint)
            t.srcFoldersJson.set(srcJson)
            t.stringFilesJson.set(strJson)
            t.assetsFilesJson.set(assetsJson)
        }
        tasks.register(
            "stringcareAfterMergeAssets$variantCapitalized",
            RestoreAssetsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.skip.set(config.skip)
        }
    }

    // AGP registers merge/process tasks after variant API callbacks; use afterEvaluate + findByName (nullable).
    afterEvaluate {
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
