package dev.vyp.stringcare.plugin.infrastructure.parsers

import dev.vyp.stringcare.plugin.domain.models.StringEntity
import java.io.File

/** Public entry for `strings.xml` parsing (SAX + optional DOM fallback). */
object XmlParser {
    /**
     * Parses all obfuscatable string entries from [file].
     *
     * @param file XML file to parse.
     * @param domFallback Parser to use when SAX cannot preserve nested markup semantics.
     * @return Ordered list of obfuscatable [StringEntity] values.
     */
    fun parse(
        file: File,
        domFallback: (File) -> List<StringEntity>,
    ): List<StringEntity> = parseStringsXml(file, domFallback)
}
