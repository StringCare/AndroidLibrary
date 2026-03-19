package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.models.ResourceFile
import dev.vyp.stringcare.plugin.models.SAttribute
import dev.vyp.stringcare.plugin.models.StringEntity
import org.w3c.dom.Element
import java.io.File

fun locateResourceFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
): List<ResourceFile> {
    if (configuration.debug) {
        println("== RESOURCE FILES FOUND ======================================")
    }
    val normalized = configuration.normalize()
    return File(projectPath)
        .walkTopDown()
        .onEnter { dir ->
            val path = dir.path.replace('\\', '/')
            !path.contains("/build/") &&
                !path.contains("/.gradle/") &&
                !path.contains("/.git/") &&
                !path.contains("/node_modules/")
        }.filter { file ->
            !file.isDirectory && file.validForXMLConfiguration(normalized)
        }.mapNotNull { file ->
            file.resourceFile(normalized)
        }.toList()
}

fun backupResourceFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
    tempRoot: String,
): List<ResourceFile> {
    val files = locateResourceFiles(projectPath, configuration.normalize())
    files.forEach { resource ->
        resource.backup(tempRoot)
    }
    return files
}

/** Test convenience: uses [StringCareSession] temp root set by [tempPath]. */
fun backupResourceFiles(
    projectPath: String,
    configuration: StringCareConfiguration,
): List<ResourceFile> = backupResourceFiles(projectPath, configuration, StringCareSession.requireTestTempRoot())

/** Test convenience: uses [StringCareSession] temp root set by [tempPath]. */
fun restoreResourceFiles(
    projectPath: String,
    module: String,
): List<File> {
    val tempRoot = StringCareSession.requireTestTempRoot()
    val out = restoreResourceFiles(projectPath, module, tempRoot)
    StringCarePlugin.resetFolder()
    return out
}

fun restoreResourceFiles(
    projectPath: String,
    module: String,
    tempRoot: String,
): List<File> {
    val resourceFiles =
        File("$tempRoot${File.separator}$module")
            .walkTopDown()
            .toList()
            .filter { file ->
                !file.isDirectory
            }.map { file ->
                file.restore(projectPath, tempRoot)
            }
    return resourceFiles
}

fun parseXML(file: File): List<StringEntity> {
    val entities = mutableListOf<StringEntity>()
    val doc = file.getXML()
    val stringNodes = doc.getElementsByTagName("string")
    for (i in 0 until stringNodes.length) {
        val node = stringNodes.item(i) as Element
        var name = ""
        val attributes = mutableListOf<SAttribute>()
        var obfuscate = false
        var androidTreatment = true
        var containsHtml = false
        val attrs = node.attributes
        for (j in 0 until attrs.length) {
            val attribute = attrs.item(j)
            val attrName = attribute.nodeName
            val attrVal = attribute.nodeValue ?: ""
            when (attrName) {
                "name" -> name = attrVal
                "hidden" -> if (attrVal != "false") obfuscate = true
                "androidTreatment" -> if (attrVal == "false") androidTreatment = false
                "containsHtml" -> if (attrVal != "false") containsHtml = true
            }
            attributes.add(SAttribute(attrName, attrVal))
        }
        if (obfuscate) {
            entities.add(
                StringEntity(
                    name,
                    attributes,
                    when {
                        containsHtml -> node.extractHtml()
                        else -> node.textContent
                    },
                    "string",
                    i,
                    androidTreatment,
                ),
            )
        }
    }
    return entities
}

fun modifyXML(
    file: File,
    key: String,
    configuration: StringCareConfiguration,
) {
    val stringEntities = parseXML(file)
    if (configuration.debug) {
        PrintUtils.print(null, file.getContent(), true)
    }

    val doc = file.getXML()
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        val entity =
            stringEntities.find {
                it.tag == "string" && it.index == i
            }
        entity?.let {
            node.textContent = obfuscateStringEntity(key, it, configuration.applicationId).value
        }
    }

    file.updateXML(doc)
    file.removeAttributes()
    if (configuration.debug) {
        PrintUtils.print(null, file.getContent(), true)
    }
}

fun obfuscateStringEntity(
    key: String,
    entity: StringEntity,
    mockId: String,
): StringEntity {
    val obfuscation =
        Stark
            .obfuscate(
                key,
                when (entity.androidTreatment) {
                    true -> entity.value.androidTreatment()
                    false -> entity.value.unescape()
                }.toByteArray(),
                mockId,
            ).toReadableString()
    return StringEntity(entity.name, entity.attributes, obfuscation, entity.tag, entity.index, entity.androidTreatment)
}

fun revealStringEntity(
    key: String,
    entity: StringEntity,
    mockId: String,
): StringEntity {
    val arr: ByteArray =
        entity.value
            .split(", ")
            .map { it.toInt().toByte() }
            .toByteArray()
    val original = String(Stark.reveal(key, arr, mockId))
    return StringEntity(entity.name, entity.attributes, original, entity.tag, entity.index, entity.androidTreatment)
}
