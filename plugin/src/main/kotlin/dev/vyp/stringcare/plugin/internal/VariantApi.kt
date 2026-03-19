package dev.vyp.stringcare.plugin.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import dev.vyp.stringcare.plugin.tasks.ObfuscateAssetsTask
import dev.vyp.stringcare.plugin.tasks.ObfuscateStringsTask
import dev.vyp.stringcare.plugin.tasks.RestoreAssetsTask
import dev.vyp.stringcare.plugin.tasks.RestoreStringsTask
import dev.vyp.stringcare.plugin.infrastructure.gradle.AndroidTaskNames
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection

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

        val mergeRes =
            "${AndroidTaskNames.MERGE}${variantCapitalized}${AndroidTaskNames.RESOURCES}"
        val beforeRes = "stringcareBeforeMergeResources$variantCapitalized"
        val afterRes = "stringcareAfterMergeResources$variantCapitalized"
        val processRes =
            "${AndroidTaskNames.PROCESS}${variantCapitalized}${AndroidTaskNames.RESOURCES}"
        namedLike(mergeRes).configureEach { task -> task.dependsOn(beforeRes) }
        namedLike(afterRes).configureEach { task -> task.dependsOn(mergeRes) }
        namedLike(processRes).configureEach { task -> task.dependsOn(afterRes) }

        val genAssets =
            "${AndroidTaskNames.GENERATE}${variantCapitalized}${AndroidTaskNames.ASSETS}"
        val beforeAssets = "stringcareBeforeMergeAssets$variantCapitalized"
        val mergeAssets =
            "${AndroidTaskNames.MERGE}${variantCapitalized}${AndroidTaskNames.ASSETS}"
        val afterAssets = "stringcareAfterMergeAssets$variantCapitalized"
        namedLike(genAssets).configureEach { task -> task.dependsOn(beforeAssets) }
        namedLike(afterAssets).configureEach { task -> task.dependsOn(mergeAssets) }
        namedLike(
            "${AndroidTaskNames.COMPRESS}${variantCapitalized}${AndroidTaskNames.ASSETS}",
        ).configureEach { task -> task.dependsOn(afterAssets) }
    }
}

private fun Project.namedLike(name: String): TaskCollection<Task> = tasks.matching { it.name == name }
