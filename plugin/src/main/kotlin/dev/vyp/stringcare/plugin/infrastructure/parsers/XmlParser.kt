package dev.vyp.stringcare.plugin.infrastructure.parsers

import dev.vyp.stringcare.plugin.domain.models.StringEntity
import java.io.File

/** Public entry for `strings.xml` parsing (SAX + optional DOM fallback). */
object XmlParser {
    fun parse(
        file: File,
        domFallback: (File) -> List<StringEntity>,
    ): List<StringEntity> = parseStringsXml(file, domFallback)
}
