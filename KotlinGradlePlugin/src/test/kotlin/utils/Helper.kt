package utils

import components.Os
import components.getContent
import components.getOs
import components.version
import java.io.File
import java.io.FileWriter

fun modifyForTest(directory: String, projectPath: String) {
    val current = File(".")
    val file = File("$directory${File.separator}$projectPath${File.separator}build.gradle")
    val content = file.getContent().replace(
        "classpath \"io.github.stringcare:plugin:\$stringcare_version\"",
        when (getOs()) {
            Os.WINDOWS -> "\nclasspath files(\"${current.absolutePath.replace("\\", "\\\\")}${File.separator}${File.separator}build${File.separator}${File.separator}libs${File.separator}${File.separator}plugin-$version.jar\")\n"
            Os.OSX -> "\nclasspath files(\"${current.absolutePath}${File.separator}build${File.separator}libs${File.separator}plugin-$version.jar\")\n"
        }
    )
    FileWriter(file.absolutePath).use { it.write(content) }

}