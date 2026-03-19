package dev.vyp.stringcare.plugin.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCareExtension
import dev.vyp.stringcare.plugin.services.StringCareBuildService
import dev.vyp.stringcare.plugin.tasks.SCPreview
import dev.vyp.stringcare.plugin.tasks.SCTestObfuscation
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import java.io.File

/** Root directory of this Gradle project (the module applying the plugin). */
fun Project.absolutePath(): String = projectDir.absolutePath

fun Project.module(): String {
    val fPath =
        this.file("build.gradle").absolutePath.replace(
            "build.gradle",
            emptyChar,
        )
    val pathComponents = fPath.split(File.separator)
    return pathComponents[pathComponents.size - 2]
}

fun Project.createExtension(): StringCareExtension = extensions.create(extensionName, StringCareExtension::class.java)

fun Project.applicationVariants(): DomainObjectSet<ApplicationVariant>? {
    if (this.plugins.hasPlugin(AppPlugin::class.java)) {
        val extension = this.extensions.getByType(AppExtension::class.java)
        return extension.applicationVariants
    }
    return null
}

fun Project.config(extension: StringCareExtension): StringCareConfiguration {
    val mod = this.module()
    return StringCareConfiguration(mod).apply {
        debug = extension.debug
        skip = extension.skip
        if (extension.srcFolders.isNotEmpty()) {
            srcFolders.addAll(extension.srcFolders)
        }
        if (extension.stringFiles.isNotEmpty()) {
            stringFiles.addAll(extension.stringFiles)
        }
        if (extension.assetsFiles.isNotEmpty()) {
            assetsFiles.addAll(extension.assetsFiles)
        }
        if (extension.mockedFingerprint.isNotEmpty()) {
            mockedFingerprint = extension.mockedFingerprint
        }
    }
}

fun Project.registerTask(
    configuration: StringCareConfiguration,
    buildService: Provider<StringCareBuildService>,
) {
    val variantDefault = buildService.get().firstVariantKeyOrDefault("debug")
    val srcJson = toJsonStringList(configuration.srcFolders)
    val strJson = toJsonStringList(configuration.stringFiles)
    val assetsJson = toJsonStringList(configuration.assetsFiles)

    this.tasks.register(gradleTaskNameDoctor, SCPreview::class.java) { t ->
        (t as Task).usesService(buildService)
        t.stringCareService.set(buildService)
        t.module.set(configuration.name)
        t.applicationId.set(configuration.applicationId)
        t.variantName.set(variantDefault)
        t.srcFolders.set(srcJson)
        t.stringFiles.set(strJson)
        t.assetsFiles.set(assetsJson)
        t.mockedFingerprint.set(configuration.mockedFingerprint)
        t.debug.set(configuration.debug)
        t.skip.set(configuration.skip)
    }
    this.tasks.register(gradleTaskNameObfuscate, SCTestObfuscation::class.java) { t ->
        (t as Task).usesService(buildService)
        t.stringCareService.set(buildService)
        t.module.set(configuration.name)
        t.applicationId.set(configuration.applicationId)
        t.variantName.set(variantDefault)
        t.srcFolders.set(srcJson)
        t.stringFiles.set(strJson)
        t.assetsFiles.set(assetsJson)
        t.mockedFingerprint.set(configuration.mockedFingerprint)
        t.debug.set(configuration.debug)
        t.skip.set(configuration.skip)
    }
}
