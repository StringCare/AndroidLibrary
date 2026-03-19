package dev.vyp.stringcare.plugin.internal

import dev.vyp.stringcare.plugin.StringCareConfiguration
import dev.vyp.stringcare.plugin.StringCarePlugin
import dev.vyp.stringcare.plugin.domain.models.ResourceFile
import dev.vyp.stringcare.plugin.domain.models.SAttribute
import dev.vyp.stringcare.plugin.domain.models.StringEntity
import dev.vyp.stringcare.plugin.infrastructure.parsers.XmlAttributes
import dev.vyp.stringcare.plugin.infrastructure.parsers.XmlParser
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

/**
 * Parses obfuscatable `<string>` nodes from [file].
 *
 * Uses SAX by default through [XmlParser] and falls back to DOM parsing when nested tags are present.
 */
fun parseXML(file: File): List<StringEntity> = XmlParser.parse(file, ::parseStringsXmlDom)

/** DOM fallback parser for `strings.xml` files that contain nested markup inside `<string>`. */
internal fun parseStringsXmlDom(file: File): List<StringEntity> {
    val doc = file.getXML()
    val stringNodes = doc.getElementsByTagName(XmlAttributes.TAG_STRING)
    return buildList {
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i) as Element
            var name = ""
            val attrs = node.attributes
            val attributes =
                buildList {
                    for (j in 0 until attrs.length) {
                        val attribute = attrs.item(j)
                        val attrName = attribute.nodeName
                        val attrVal = attribute.nodeValue ?: ""
                        add(SAttribute(attrName, attrVal))
                    }
                }
            var obfuscate = false
            var androidTreatment = true
            var containsHtml = false
            for (attr in attributes) {
                when (attr.name) {
                    XmlAttributes.NAME -> name = attr.value
                    XmlAttributes.HIDDEN -> if (attr.value != "false") obfuscate = true
                    XmlAttributes.ANDROID_TREATMENT -> if (attr.value == "false") androidTreatment = false
                    XmlAttributes.CONTAINS_HTML -> if (attr.value != "false") containsHtml = true
                }
            }
            if (obfuscate) {
                add(
                    StringEntity(
                        name,
                        attributes,
                        when {
                            containsHtml -> node.extractHtml()
                            else -> node.textContent
                        },
                        XmlAttributes.TAG_STRING,
                        i,
                        androidTreatment,
                    ),
                )
            }
        }
    }
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
    val stringNodeList = doc.getElementsByTagName(XmlAttributes.TAG_STRING)
    for (i in 0 until stringNodeList.length) {
        val node = stringNodeList.item(i)
        val entity =
            stringEntities.find {
                it.tag == XmlAttributes.TAG_STRING && it.index == i
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
    return entity.withValue(obfuscation)
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
    return entity.withValue(original)
}
