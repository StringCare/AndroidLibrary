package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.domain.models.AssetsFile
import dev.vyp.stringcare.plugin.domain.models.ExecutionResult
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import groovy.json.StringEscapeUtils
import org.gradle.api.GradleException
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun String.runCommand(runner: (command: String, result: String) -> Unit = { _, _ -> }): String {
    val outcome =
        execute(this).getOrElse { error ->
            throw GradleException("Unable to execute command: $this", error)
        }
    return when (outcome) {
        is ExecutionResult.Success -> {
            runner(outcome.command, outcome.result)
            outcome.result
        }
        is ExecutionResult.Timeout -> {
            throw GradleException(
                "Command timed out: ${outcome.command}\n${outcome.result}",
            )
        }
        is ExecutionResult.Failure -> {
            throw GradleException(
                "Command failed (exit=${outcome.exitCode}): ${outcome.command}\n${outcome.result}",
                outcome.error,
            )
        }
    }
}

fun String.normalize(): String {
    val collapsed =
        replace("\n", " ")
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    return if (collapsed == this) this else collapsed
}

fun String.normalizePath(): String {
    val unixPath =
        this
            .replace("\\", "/")
            .replace("\\\\", "/")
            .replace("//", "/")
    return when (getOs()) {
        Os.OSX, Os.LINUX -> unixPath
        Os.WINDOWS -> unixPath.replace("/", "\\")
    }
}

fun String.uncapitalize(): String {
    val original = this.trim()
    if (original.isEmpty()) {
        return ""
    }
    val char = original[0].lowercaseChar()
    return when {
        original.length == 1 -> char.toString()
        else -> char + original.substring(1, original.length)
    }
}

fun String.escape(): String = Regex.escape(this)

fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)

fun String.removeNewLines(): String = this.replace("\n", "")

fun String.androidTreatment(): String {
    val words = this.split(" ")
    return buildList {
        words.forEach { value ->
            if (value.isNotBlank()) {
                add(value.trim())
            }
        }
    }.joinToString(separator = " ")
}

fun File.validForXMLConfiguration(configuration: StringCareConfiguration): Boolean {
    var valid =
        this.absolutePath.contains("${File.separator}${configuration.name}${File.separator}") &&
            excludedForXML().not()
    if (valid) {
        valid = false
        configuration.srcFolders.forEach { folder ->
            if (this.absolutePath.contains(
                    "${File.separator}$folder${File.separator}".replace(
                        "${File.separator}${File.separator}",
                        File.separator,
                    ),
                )
            ) {
                valid = true
            }
        }
    }
    if (valid) {
        valid = false
        configuration.stringFiles.forEach { file ->
            if (this.absolutePath.contains(
                    "${File.separator}$file".replace(
                        "${File.separator}${File.separator}",
                        File.separator,
                    ),
                )
            ) {
                valid = true
            }
        }
    }
    if (configuration.debug && excludedForXML().not() && valid) {
        println("✔ valid file ${this.absolutePath}")
    }
    return valid
}

fun File.validForAssetsConfiguration(configuration: StringCareConfiguration): Boolean {
    var valid =
        this.absolutePath.contains("${File.separator}${configuration.name}${File.separator}") &&
            excludedForAssets().not()
    if (valid) {
        valid = false
        configuration.assetsFiles.forEach { file ->
            if (this.absolutePath.endsWith(
                    "${File.separator}$file".replace(
                        "${File.separator}${File.separator}",
                        File.separator,
                    ),
                ) ||
                (file.contains("*.") && this.absolutePath.endsWith(file.replace("*", "")))
            ) {
                valid = true
            }
        }
    }
    if (configuration.debug && excludedForAssets().not() && valid) {
        println("✔ valid file ${this.absolutePath}")
    }
    return valid
}

val exclude =
    listOf(
        "/build/",
        "/.git/",
        "/.idea/",
        "/.gradle/",
        "/gradle/",
    )

fun File.excludedForXML(): Boolean {
    var valid = true
    exclude.forEach { value ->
        when {
            this.absolutePath.contains(value.normalizePath()) -> valid = false
        }
    }
    return (valid && this.isDirectory.not() && this.absolutePath.contains(".xml")).not()
}

fun File.excludedForAssets(): Boolean {
    var valid = true
    exclude.forEach { value ->
        when {
            this.absolutePath.contains(value.normalizePath()) -> valid = false
        }
    }
    return (valid && this.isDirectory.not() && this.absolutePath.contains("${File.separator}assets${File.separator}")).not()
}

fun File.resourceFile(configuration: StringCareConfiguration): ResourceFile? {
    var sourceFolder = ""
    var validFile: File? = null
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains(
                "${File.separator}$folder${File.separator}".replace(
                    "${File.separator}${File.separator}",
                    File.separator,
                ),
            )
        ) {
            sourceFolder = folder
            valid = true
        }
    }
    if (valid) {
        valid = false
        configuration.stringFiles.forEach { file ->
            if (this.absolutePath.contains(
                    "${File.separator}$file".replace(
                        "${File.separator}${File.separator}",
                        File.separator,
                    ),
                )
            ) {
                valid = true
                validFile = this
            }
        }
    }
    val file = validFile ?: return null
    return if (valid) ResourceFile(file, sourceFolder, configuration.name) else null
}

fun File.assetsFile(configuration: StringCareConfiguration): AssetsFile? {
    var sourceFolder = ""
    var validFile: File? = null
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains(
                "${File.separator}$folder${File.separator}".replace(
                    "${File.separator}${File.separator}",
                    File.separator,
                ),
            )
        ) {
            sourceFolder = folder
            valid = true
        }
    }
    if (valid) {
        valid = false
        configuration.assetsFiles.forEach { file ->
            if (this.absolutePath.endsWith(
                    "${File.separator}$file".replace(
                        "${File.separator}${File.separator}",
                        File.separator,
                    ),
                ) ||
                (file.contains("*.") && this.absolutePath.endsWith(file.replace("*", "")))
            ) {
                valid = true
                validFile = this
            }
        }
    }
    if (configuration.debug && excludedForAssets().not()) {
        println(
            "${when {
                valid -> "valid file"
                else -> ""
            }}${when {
                validFile != null -> validFile?.getContent()
                else -> "the file is null"
            }}",
        )
    }
    val file = validFile ?: return null
    return if (valid) AssetsFile(file, sourceFolder, configuration.name) else null
}

fun Process.outputString(): String {
    val input = this.inputStream.bufferedReader().use { it.readText() }
    val error = this.errorStream.bufferedReader().use { it.readText() }
    return "$input \n $error".replace("\r", "")
}

fun defaultConfig(): StringCareConfiguration =
    StringCareConfiguration("app").apply {
        stringFiles.add("strings.xml")
        srcFolders.add("src/main")
    }

fun File.restore(
    projectPath: String,
    tempRoot: String,
): File {
    val cleanPath =
        "$projectPath${File.separator}${this.absolutePath.split(tempRoot)[1]}"
            .replace("${File.separator}${File.separator}", File.separator)

    val restore = File(cleanPath)
    if (restore.exists()) {
        restore.delete()
    }

    File(restore.absolutePath).writeText(this.getContent())

    return restore
}

fun File.getXML(): Document {
    val inputStream = FileInputStream(this)
    val reader = InputStreamReader(inputStream, "UTF-8")
    val inputSource = InputSource(reader)
    inputSource.encoding = "UTF-8"

    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    return docBuilder.parse(inputSource)
}

fun File.updateXML(document: Document) {
    val output = StringWriter()
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    transformer.transform(DOMSource(document), StreamResult(output))
    val xml = output.toString()
    this.writeText(xml)
}

fun File.removeAttributes() {
    val content =
        this
            .getContent()
            .replace("hidden=\"true\"", "")
            .replace("hidden=\"false\"", "")
            .replace("containsHtml=\"true\"", "")
            .replace("containsHtml=\"false\"", "")
            .replace("androidTreatment=\"true\"", "")
            .replace("androidTreatment=\"false\"", "")
    this.writeText(content)
    updateXML(this.getXML())
}

fun File.getContent() = this.inputStream().readBytes().toString(Charsets.UTF_8)

fun StringCareConfiguration.normalize(): StringCareConfiguration {
    val nextStrings = stringFiles.map { it.normalizePath() }
    val nextSrc = srcFolders.map { it.normalizePath() }
    if (nextStrings == stringFiles && nextSrc == srcFolders) {
        return this
    }
    stringFiles.clear()
    stringFiles.addAll(nextStrings)
    srcFolders.clear()
    srcFolders.addAll(nextSrc)
    return this
}
