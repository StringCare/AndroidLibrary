package components

import StringCare
import StringCare.Configuration
import models.AssetsFile
import java.io.File

fun locateAssetsFiles(projectPath: String, configuration: Configuration): List<AssetsFile> {
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

fun backupAssetsFiles(projectPath: String, configuration: Configuration): List<AssetsFile> {
    val files = locateAssetsFiles(projectPath, configuration.normalize())
    files.forEach { resource ->
        resource.backup()
    }
    return files
}

fun restoreAssetsFiles(projectPath: String, module: String): List<File> {
    val resourceFiles = File("${StringCare.tempFolder}${File.separator}$module")
        .walkTopDown().toList().filter { file ->
            !file.isDirectory
        }.map {
            it.restore(projectPath)
        }
    StringCare.resetFolder()
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
