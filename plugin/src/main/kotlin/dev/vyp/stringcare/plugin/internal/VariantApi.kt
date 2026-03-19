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
            (t as ObfuscateStringsTask).stringCareService.set(buildService)
            t.projectPath = absoluteProjectPath
            t.moduleName = moduleNameValue
            t.variantName = variantName
            t.applicationId = applicationId
            t.skip = config.skip
            t.debug = config.debug
            t.mockedFingerprint = config.mockedFingerprint
            t.srcFoldersJson = toJsonStringList(config.srcFolders)
            t.stringFilesJson = toJsonStringList(config.stringFiles)
            t.assetsFilesJson = toJsonStringList(config.assetsFiles)
        }
        tasks.register(
            "stringcareAfterMergeResources$variantCapitalized",
            RestoreStringsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            (t as RestoreStringsTask).stringCareService.set(buildService)
            t.projectPath = absoluteProjectPath
            t.moduleName = moduleNameValue
            t.skip = config.skip
        }
        variantNames.add(variantCapitalized)

        tasks.register(
            "stringcareBeforeMergeAssets$variantCapitalized",
            ObfuscateAssetsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            (t as ObfuscateAssetsTask).stringCareService.set(buildService)
            t.projectPath = absoluteProjectPath
            t.moduleName = moduleNameValue
            t.variantName = variantName
            t.applicationId = applicationId
            t.skip = config.skip
            t.debug = config.debug
            t.mockedFingerprint = config.mockedFingerprint
            t.srcFoldersJson = toJsonStringList(config.srcFolders)
            t.stringFilesJson = toJsonStringList(config.stringFiles)
            t.assetsFilesJson = toJsonStringList(config.assetsFiles)
        }
        tasks.register(
            "stringcareAfterMergeAssets$variantCapitalized",
            RestoreAssetsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            (t as RestoreAssetsTask).stringCareService.set(buildService)
            t.projectPath = absoluteProjectPath
            t.moduleName = moduleNameValue
            t.skip = config.skip
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
