package dev.vyp.stringcare.plugin.infrastructure.parsers

import dev.vyp.stringcare.plugin.domain.models.SAttribute
import dev.vyp.stringcare.plugin.domain.models.StringEntity
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.SAXParserFactory

/**
 * SAX-first `strings.xml` parsing with DOM fallback when `<string>` contains nested elements.
 *
 * @param domParser full DOM implementation (provided by plugin `internal` layer to reuse [File.getXML] / [Node.extractHtml]).
 */
fun parseStringsXml(
    file: File,
    domParser: (File) -> List<StringEntity>,
): List<StringEntity> =
    if (stringsXmlNeedsDomFallback(file)) {
        domParser(file)
    } else {
        parseStringsXmlSax(file)
    }

private fun stringsXmlNeedsDomFallback(file: File): Boolean {
    val detector = DomFallbackDetector()
    return runCatching {
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.newSAXParser().parse(InputSource(FileInputStream(file)), detector)
        detector.needsDom
    }.getOrElse { true }
}

private class DomFallbackDetector : DefaultHandler() {
    private var insideString = false
    var needsDom = false

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
    ) {
        val tag = localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfterLast(':') ?: ""
        when {
            tag == XmlAttributes.TAG_STRING -> insideString = true
            insideString -> needsDom = true
        }
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
    ) {
        val tag = localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfterLast(':') ?: ""
        if (tag == XmlAttributes.TAG_STRING) {
            insideString = false
        }
    }
}

private fun parseStringsXmlSax(file: File): List<StringEntity> {
    val handler = StringsSaxHandler()
    val factory = SAXParserFactory.newInstance()
    factory.isNamespaceAware = true
    factory.newSAXParser().parse(InputSource(FileInputStream(file)), handler)
    return handler.entities
}

/** SAX path: `<string>` bodies must be plain text (no nested tags). */
private class StringsSaxHandler : DefaultHandler() {
    val entities = mutableListOf<StringEntity>()
    private var inString = false
    private var stringIndexAmongAllStrings = 0
    private var currentName = ""
    private val currentAttributes = mutableListOf<SAttribute>()
    private var obfuscate = false
    private var androidTreatment = true
    private var containsHtml = false
    private val textBuffer = StringBuilder()

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
    ) {
        val tag = localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfterLast(':') ?: ""
        if (tag != XmlAttributes.TAG_STRING) {
            return
        }
        inString = true
        textBuffer.clear()
        currentName = ""
        currentAttributes.clear()
        obfuscate = false
        androidTreatment = true
        containsHtml = false
        if (attributes != null) {
            for (i in 0 until attributes.length) {
                val attrName = attributes.getQName(i)
                val attrVal = attributes.getValue(i) ?: ""
                currentAttributes.add(SAttribute(attrName, attrVal))
                when (attrName) {
                    XmlAttributes.NAME -> currentName = attrVal
                    XmlAttributes.HIDDEN -> if (attrVal != "false") obfuscate = true
                    XmlAttributes.ANDROID_TREATMENT -> if (attrVal == "false") androidTreatment = false
                    XmlAttributes.CONTAINS_HTML -> if (attrVal != "false") containsHtml = true
                }
            }
        }
    }

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int,
    ) {
        if (inString) {
            textBuffer.append(ch, start, length)
        }
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
    ) {
        val tag = localName?.takeIf { it.isNotEmpty() } ?: qName?.substringAfterLast(':') ?: ""
        if (tag != XmlAttributes.TAG_STRING || !inString) {
            return
        }
        inString = false
        if (obfuscate) {
            if (containsHtml) {
                // Should have been routed to DOM fallback; treat as plain text if we ever get here.
            }
            entities.add(
                StringEntity(
                    name = currentName,
                    attributes = currentAttributes.toList(),
                    value = textBuffer.toString(),
                    tag = XmlAttributes.TAG_STRING,
                    index = stringIndexAmongAllStrings,
                    androidTreatment = androidTreatment,
                ),
            )
        }
        stringIndexAmongAllStrings++
    }
}
