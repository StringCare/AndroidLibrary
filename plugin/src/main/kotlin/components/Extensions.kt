package components

import StringCare
import StringCare.Configuration
import StringCare.Extension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.google.gson.Gson
import groovy.json.StringEscapeUtils
import models.AssetsFile
import models.ResourceFile
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import task.SCPreview
import task.SCTestObfuscation
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
    val result = execute(this)
    runner(result.command, result.result)
    return result.result
}

fun String.normalize(): String {
    val com = mutableListOf<String>()
    this.replace("\n", " ").split(" ").forEach {
        if (it.trim().isNotEmpty()) {
            com.add(it)
        }
    }
    return com.joinToString(" ")
}

fun String.normalizePath(): String {
    val unixPath = this.replace("\\", "/")
        .replace("\\\\", "/")
        .replace("//", "/")
    return when (getOs()) {
        Os.OSX -> unixPath
        Os.WINDOWS -> unixPath.replace("/", "\\")
    }
}

fun String.uncapitalize(): String {
    val original = this.trim()
    if (original.isEmpty()) {
        return ""
    }
    val char = original[0].toLowerCase()
    return when {
        original.length == 1 -> char.toString()
        else -> char + original.substring(1, original.length)
    }
}

fun String.escape(): String = Regex.escape(this)
fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
fun String.removeNewLines(): String = this.replace("\n", "")
fun String.androidTreatment(): String {
    val va = this.split(" ")
    val values = mutableListOf<String>()
    va.forEach { value ->
        if (value.trim().isNotBlank()) {
            values.add(value.trim())
        }
    }
    return values.joinToString(separator = " ")
}

fun File.validForXMLConfiguration(configuration: Configuration): Boolean {
    var valid = this.absolutePath.contains("${File.separator}${configuration.name}${File.separator}")
            && excludedForXML().not()
    if (valid) {
        valid = false
        configuration.srcFolders.forEach { folder ->
            if (this.absolutePath.contains(
                    "${File.separator}$folder${File.separator}".replace(
                        "${File.separator}${File.separator}",
                        File.separator
                    )
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
                        File.separator
                    )
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

fun File.validForAssetsConfiguration(configuration: Configuration): Boolean {
    var valid = this.absolutePath.contains("${File.separator}${configuration.name}${File.separator}")
            && excludedForAssets().not()
    if (valid) {
        valid = false
        configuration.assetsFiles.forEach { file ->
            if (this.absolutePath.endsWith(
                    "${File.separator}$file".replace(
                        "${File.separator}${File.separator}",
                        File.separator
                    )
                )
                || (file.contains("*.") && this.absolutePath.endsWith(file.replace("*", "")))
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

val exclude = listOf(
    "/build/",
    "/.git/",
    "/.idea/",
    "/.gradle/",
    "/gradle/"
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

fun File.resourceFile(configuration: Configuration): ResourceFile? {
    var sourceFolder = ""
    var validFile: File? = null
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains(
                "${File.separator}$folder${File.separator}".replace(
                    "${File.separator}${File.separator}",
                    File.separator
                )
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
                        File.separator
                    )
                )
            ) {
                valid = true
                validFile = this
            }
        }
    }
    return if (valid) ResourceFile(validFile!!, sourceFolder, configuration.name) else null
}

fun File.assetsFile(configuration: Configuration): AssetsFile? {
    var sourceFolder = ""
    var validFile: File? = null
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains(
                "${File.separator}$folder${File.separator}".replace(
                    "${File.separator}${File.separator}",
                    File.separator
                )
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
                        File.separator
                    )
                )
                || (file.contains("*.") && this.absolutePath.endsWith(file.replace("*", "")))
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
            }}"
        )
    }
    return if (valid) AssetsFile(validFile!!, sourceFolder, configuration.name) else null
}

fun Project.absolutePath(): String {
    val fPath = this.file("build.gradle").absolutePath.replace(
        "build.gradle",
        emptyChar
    )
    val p = fPath.split(File.separator)
    return fPath.replace(p[p.size - 2] + File.separator, "")
}

fun Project.module(): String {
    val fPath = this.file("build.gradle").absolutePath.replace(
        "build.gradle",
        emptyChar
    )
    val p = fPath.split(File.separator)
    return p[p.size - 2]
}

fun Project.createExtension(): Extension {
    return extensions.create(extensionName, Extension::class.java)
}

fun Project.applicationVariants(): DomainObjectSet<ApplicationVariant>? {
    if (this.plugins.hasPlugin(AppPlugin::class.java)) {
        val extension = this.extensions.getByType(AppExtension::class.java)
        return extension.applicationVariants
    }
    return null
}

fun Project.config(extension: Extension): Configuration {
    val mod = this.module()
    return Configuration(mod).apply {
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

fun Project.registerTask(configuration: Configuration) {
    val gson = Gson()

    this.tasks.register(gradleTaskNameDoctor, SCPreview::class.java)
    this.tasks.register(gradleTaskNameObfuscate, SCTestObfuscation::class.java)

    val previewTask = this.tasks.getByPath(gradleTaskNameDoctor) as SCPreview
    previewTask.module = configuration.name
    previewTask.applicationId = configuration.applicationId
    previewTask.srcFolders = gson.toJson(configuration.srcFolders)
    previewTask.stringFiles = gson.toJson(configuration.stringFiles)
    previewTask.assetsFiles = gson.toJson(configuration.assetsFiles)
    previewTask.mockedFingerprint = configuration.mockedFingerprint
    previewTask.debug = configuration.debug
    previewTask.skip = configuration.skip

    val obfuscateTask = this.tasks.getByPath(gradleTaskNameObfuscate) as SCTestObfuscation
    obfuscateTask.module = configuration.name
    obfuscateTask.applicationId = configuration.applicationId
    obfuscateTask.srcFolders = gson.toJson(configuration.srcFolders)
    obfuscateTask.stringFiles = gson.toJson(configuration.stringFiles)
    obfuscateTask.assetsFiles = gson.toJson(configuration.assetsFiles)
    obfuscateTask.mockedFingerprint = configuration.mockedFingerprint
    obfuscateTask.debug = configuration.debug
    obfuscateTask.skip = configuration.skip
}

fun Process.outputString(): String {
    val input = this.inputStream.bufferedReader().use { it.readText() }
    val error = this.errorStream.bufferedReader().use { it.readText() }
    return "$input \n $error".replace("\r", "")
}

fun defaultConfig(): Configuration {
    return Configuration("app").apply {
        stringFiles.add("strings.xml")
        srcFolders.add("src/main")
    }
}

fun ResourceFile.backup(): File {
    val cleanPath =
        "${StringCare.tempFolder}${File.separator}${this.module}${File.separator}${this.sourceFolder}${this.file.absolutePath.split(
            this.sourceFolder
        )[1]}"
            .replace("${File.separator}${File.separator}", File.separator)

    val backupFile = File(cleanPath)
    this.file.copyTo(backupFile, true)
    return backupFile
}

fun AssetsFile.backup(): File {
    val cleanPath =
        "${StringCare.tempFolder}${File.separator}${this.module}${File.separator}${this.sourceFolder}${this.file.absolutePath.split(
            this.sourceFolder
        )[1]}"
            .replace("${File.separator}${File.separator}", File.separator)

    val backupFile = File(cleanPath)
    this.file.copyTo(backupFile, true)
    return backupFile
}

fun File.restore(projectPath: String): File {
    val cleanPath = "$projectPath${File.separator}${this.absolutePath.split(StringCare.tempFolder)[1]}"
        .replace("${File.separator}${File.separator}", File.separator)

    val restore = File(cleanPath)
    if (restore.exists()) {
        restore.delete()
    }

    /*
    FileWriter(restore.absolutePath).use {
        it.write(this.getContent())
    }*/

    File(restore.absolutePath).writeText(this.getContent())

    return restore
}

fun ByteArray.toReadableString(): String {
    val builder = StringBuilder()
    this.forEachIndexed { index, byte ->
        if (index == this.size - 1) {
            builder.append(byte)
        } else {
            builder.append("$byte, ")
        }
    }
    return builder.toString()
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
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.transform(DOMSource(document), StreamResult(output))
    val xml = output.toString()
    this.writeText(xml)
    //FileWriter(this.absolutePath).use { it.write(xml) }
}

fun File.removeAttributes() {
    val content = this.getContent()
        .replace("hidden=\"true\"", "")
        .replace("hidden=\"false\"", "")
        .replace("containsHtml=\"true\"", "")
        .replace("containsHtml=\"false\"", "")
        .replace("androidTreatment=\"true\"", "")
        .replace("androidTreatment=\"false\"", "")
    // FileWriter(this.absolutePath).use { it.write(content) }
    this.writeText(content)
    updateXML(this.getXML())
}


fun File.getContent() = this.inputStream().readBytes().toString(Charsets.UTF_8)

fun Task.getModuleName(): String? {
    val path = this.project.path
    return if (path.isEmpty()) null else path.split(":")[path.split(":").size - 1]
}

fun Task.dataFound(): Boolean = this.name.contains(pre)
        && this.name.contains(build)
        && this.name != "$pre$build"
        && !this.name.contains(test)

fun Task.onMergeResourcesStarts(): Boolean = this.name.contains(merge)
        && this.name.contains(resources)
        && !this.name.contains(test)

fun Task.onMergeResourcesFinish(): Boolean = this.name.contains(merge)
        && this.name.contains(resources)
        && !this.name.contains(test)

fun Task.onMergeAssetsStarts(): Boolean = this.name.contains(generate)
        && this.name.contains(assets)
        && !this.name.contains(test)

fun Task.onMergeAssetsFinish(): Boolean = this.name.contains(merge)
        && this.name.contains(assets)
        && !this.name.contains(test)

fun Task.dataFoundVariant(): String = this.name.substring(pre.length)
    .substring(0, this.name.substring(pre.length).length - build.length)

fun Task.onMergeResourcesStartsVariant(): String = this.name.substring(merge.length)
    .substring(0, this.name.substring(merge.length).length - resources.length)

fun Task.onMergeResourcesFinishVariant(): String = this.name.substring(merge.length)
    .substring(0, this.name.substring(merge.length).length - resources.length)

fun Task.onMergeAssetsStartsVariant(): String = this.name.substring(generate.length)
    .substring(0, this.name.substring(generate.length).length - assets.length)

fun Task.onMergeAssetsFinishVariant(): String = this.name.substring(merge.length)
    .substring(0, this.name.substring(merge.length).length - assets.length)

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}

fun Node.extractHtml(): String {
    val stringBuilder = StringBuilder()
    for (i in 0 until this.childNodes.length) {
        val item = this.childNodes.item(i)
        val type = item.getType()
        when (type) {
            StringType.BR -> stringBuilder.append(
                "<br>${when {
                    item.textContent.isNotEmpty() -> item.extractHtml()
                    else -> ""
                }
                }</br>"
            )
            StringType.I -> stringBuilder.append(
                "<i>${when {
                    item.textContent.isNotEmpty() -> item.extractHtml()
                    else -> ""
                }
                }</i>"
            )
            StringType.STRONG -> stringBuilder.append(
                "<strong>${when {
                    item.textContent.isNotEmpty() -> item.extractHtml()
                    else -> ""
                }
                }</strong>"
            )
            StringType.TEXT -> stringBuilder.append(item.textContent)
        }

    }
    return stringBuilder.toString()
}

enum class StringType {
    BR,
    TEXT,
    I,
    STRONG
}

fun Node.getType(): StringType {
    return when {
        this.toString().contains("[br:") -> StringType.BR
        this.toString().contains("[i:") -> StringType.I
        this.toString().contains("[strong:") -> StringType.STRONG
        this.toString().contains("[#text") -> StringType.TEXT
        else -> StringType.TEXT
    }
}

fun Configuration.normalize(): Configuration {
    val stringFiles = mutableListOf<String>()
    val sourceFolders = mutableListOf<String>()
    this.stringFiles.forEach { file ->
        stringFiles.add(file.normalizePath())
    }
    this.srcFolders.forEach { folder ->
        sourceFolders.add(folder.normalizePath())
    }
    this.stringFiles = stringFiles
    this.srcFolders = sourceFolders
    return this
}
