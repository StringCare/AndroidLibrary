package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.models.AssetsFile
import java.io.File

fun locateAssetsFiles(projectPath: String, configuration: StringCareConfiguration): List<AssetsFile> {
    if (configuration.debug) {
        println("== ASSETS FILES FOUND ======================================")
    }
    return File(projectPath).walkTopDown()
        .filterIndexed { _, file ->
            file.validForAssetsConfiguration(configuration.normalize())
        }.map {
            it.assetsFile(configuration.normalize())!!
        }.toList()
}

fun backupAssetsFiles(projectPath: String, configuration: StringCareConfiguration): List<AssetsFile> {
    val files = locateAssetsFiles(projectPath, configuration.normalize())
    files.forEach { resource ->
        resource.backup()
    }
    return files
}

fun restoreAssetsFiles(projectPath: String, module: String): List<File> {
    val resourceFiles = File("${StringCarePlugin.tempFolder}${File.separator}$module")
        .walkTopDown().toList().filter { file ->
            !file.isDirectory
        }.map {
            it.restore(projectPath)
        }
    StringCarePlugin.resetFolder()
    return resourceFiles
}

fun obfuscateFile(key: String, file: File, mockId: String) {
    val obfuscation = Stark.obfuscate(
        key,
        file.readBytes(),
        mockId
    )
    file.writeBytes(obfuscation)
}

fun revealFile(key: String, file: File, mockId: String = "") {
    file.writeBytes(Stark.reveal(key, file.readBytes(), mockId))
}
