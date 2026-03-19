package dev.vyp.stringcare.plugin.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCareExtension
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
    bookkeeping: StringCareConfiguration,
    extension: StringCareExtension,
    buildService: Provider<StringCareBuildService>,
) {
    val androidComponents =
        extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
            ?: return

    val absoluteProjectPath = buildService.get().absoluteProjectPath()
    val moduleNameValue = bookkeeping.name
    androidComponents.onVariants(androidComponents.selector().all()) { variant ->
        val variantName: String = variant.name
        val applicationId = variant.applicationId.get()
        buildService.get().putVariantApplicationId(variantName, applicationId)
        if (bookkeeping.applicationId.isEmpty()) bookkeeping.applicationId = applicationId
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
            t.skip.set(provider { extension.skip })
            t.debug.set(provider { extension.debug })
            t.mockedFingerprint.set(provider { extension.mockedFingerprint })
            t.srcFoldersJson.set(provider { toJsonStringList(extension.srcFolders) })
            t.stringFilesJson.set(provider { toJsonStringList(extension.stringFiles) })
            t.assetsFilesJson.set(provider { toJsonStringList(extension.assetsFiles) })
            t.xmlInputFiles.from(
                provider {
                    val includes =
                        extension.srcFolders
                            .ifEmpty { listOf("src/main/res") }
                            .flatMap { src ->
                                extension.stringFiles
                                    .ifEmpty { listOf("strings.xml") }
                                    .map { name -> "$src/**/$name" }
                            }
                    fileTree(absoluteProjectPath).matching { pattern ->
                        includes.forEach { pattern.include(it) }
                        pattern.exclude("**/build/**", "**/.git/**", "**/.gradle/**", "**/node_modules/**")
                    }
                },
            )
            t.outputDirectory.set(layout.buildDirectory.dir("stringcare/obfuscate-strings/$variantName"))
        }
        tasks.register(
            "stringcareAfterMergeResources$variantCapitalized",
            RestoreStringsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.skip.set(provider { extension.skip })
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
            t.skip.set(provider { extension.skip })
            t.debug.set(provider { extension.debug })
            t.mockedFingerprint.set(provider { extension.mockedFingerprint })
            t.srcFoldersJson.set(provider { toJsonStringList(extension.srcFolders) })
            t.stringFilesJson.set(provider { toJsonStringList(extension.stringFiles) })
            t.assetsFilesJson.set(provider { toJsonStringList(extension.assetsFiles) })
        }
        tasks.register(
            "stringcareAfterMergeAssets$variantCapitalized",
            RestoreAssetsTask::class.java,
        ) { t ->
            (t as Task).usesService(buildService)
            t.stringCareService.set(buildService)
            t.projectPath.set(absoluteProjectPath)
            t.moduleName.set(moduleNameValue)
            t.skip.set(provider { extension.skip })
        }

        tasks.named("stringcareBeforeMergeAssets$variantCapitalized").configure {
            it.mustRunAfter(tasks.named("stringcareBeforeMergeResources$variantCapitalized"))
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
