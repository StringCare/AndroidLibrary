package dev.vyp.stringcare.plugin.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCareExtension
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import dev.vyp.stringcare.plugin.tasks.ObfuscateAssetsTask
import dev.vyp.stringcare.plugin.tasks.ObfuscateStringsTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider

private fun String.variantTaskSuffix(): String = replaceFirstChar { it.uppercase() }

fun Project.registerVariantObfuscationTasks(
    bookkeeping: StringCareConfiguration,
    extension: StringCareExtension,
    buildService: Provider<StringCareBuildService>,
) {
    val androidComponents =
        extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
            ?: return

    val moduleNameValue = bookkeeping.name
    androidComponents.onVariants(androidComponents.selector().all()) { variant ->
        val variantName: String = variant.name
        val applicationId = variant.applicationId.get()
        buildService.get().putVariantApplicationId(variantName, applicationId)
        if (bookkeeping.applicationId.isEmpty()) bookkeeping.applicationId = applicationId
        val variantCapitalized = variantName.variantTaskSuffix()

        val obfuscateStrings =
            tasks.register(
                "stringcareObfuscateStringResources$variantCapitalized",
                ObfuscateStringsTask::class.java,
            ) { t ->
                t.projectPath.set(layout.projectDirectory.asFile.absolutePath)
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
                        fileTree(layout.projectDirectory).matching { pattern ->
                            includes.forEach { pattern.include(it) }
                            pattern.exclude("**/build/**", "**/.git/**", "**/.gradle/**", "**/node_modules/**")
                        }
                    },
                )
                t.outputDirectory.set(
                    layout.buildDirectory.dir("intermediates/stringcare/generated-res/$variantName"),
                )
            }
        variant.sources.res?.addGeneratedSourceDirectory(
            obfuscateStrings,
            ObfuscateStringsTask::outputDirectory,
        )

        val obfuscateAssets =
            tasks.register(
                "stringcareObfuscateAssets$variantCapitalized",
                ObfuscateAssetsTask::class.java,
            ) { t ->
                t.projectPath.set(layout.projectDirectory.asFile.absolutePath)
                t.moduleName.set(moduleNameValue)
                t.variantName.set(variantName)
                t.applicationId.set(applicationId)
                t.skip.set(provider { extension.skip })
                t.debug.set(provider { extension.debug })
                t.mockedFingerprint.set(provider { extension.mockedFingerprint })
                t.srcFoldersJson.set(provider { toJsonStringList(extension.srcFolders) })
                t.stringFilesJson.set(provider { toJsonStringList(extension.stringFiles) })
                t.assetsFilesJson.set(provider { toJsonStringList(extension.assetsFiles) })
                t.assetInputFiles.from(
                    provider {
                        val patterns = extension.assetsFiles
                        if (patterns.isEmpty()) {
                            return@provider files()
                        }
                        fileTree(layout.projectDirectory).matching { tree ->
                            extension.srcFolders.ifEmpty { listOf("src/main") }.forEach { src ->
                                patterns.forEach { name ->
                                    tree.include("$src/**/$name")
                                }
                            }
                            tree.exclude("**/build/**", "**/.git/**", "**/.gradle/**", "**/node_modules/**")
                        }
                    },
                )
                t.outputDirectory.set(
                    layout.buildDirectory.dir("intermediates/stringcare/generated-assets/$variantName"),
                )
            }
        variant.sources.assets?.addGeneratedSourceDirectory(
            obfuscateAssets,
            ObfuscateAssetsTask::outputDirectory,
        )
    }
}
