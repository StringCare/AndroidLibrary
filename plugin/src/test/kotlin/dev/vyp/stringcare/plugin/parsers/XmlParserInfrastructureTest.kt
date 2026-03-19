package dev.vyp.stringcare.plugin.parsers

import dev.vyp.stringcare.plugin.internal.parseStringsXmlDom
import dev.vyp.stringcare.plugin.infrastructure.parsers.XmlParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.charset.StandardCharsets

class XmlParserInfrastructureTest {
    @Test
    fun `XmlParser parse matches hidden string extraction`() {
        val xml =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="a">x</string>
                <string name="b" hidden="true">y</string>
            </resources>
            """.trimIndent()
        val file = File.createTempFile("strings", ".xml")
        file.writeText(xml, Charsets.UTF_8)
        try {
            val entities = XmlParser.parse(file, ::parseStringsXmlDom)
            assertEquals(1, entities.size)
            assertEquals("b", entities[0].name)
            assertEquals("y", entities[0].value)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `SAX path used when no nested elements in string`() {
        val xml =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="h" hidden="true">plain</string>
            </resources>
            """.trimIndent()
        val file = File.createTempFile("strings", ".xml")
        file.writeText(xml)
        try {
            val entities = XmlParser.parse(file, ::parseStringsXmlDom)
            assertEquals(1, entities.size)
            assertEquals("plain", entities[0].value)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `parses UTF-16 hidden string`() {
        val xml =
            """
            <?xml version="1.0" encoding="utf-16"?>
            <resources>
                <string name="h" hidden="true">café</string>
            </resources>
            """.trimIndent()
        val file = File.createTempFile("strings", ".xml")
        file.writeText(xml, StandardCharsets.UTF_16)
        try {
            val entities = XmlParser.parse(file, ::parseStringsXmlDom)
            assertEquals(1, entities.size)
            assertEquals("café", entities[0].value)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `malformed XML fails parse`() {
        val file = File.createTempFile("bad", ".xml")
        file.writeText("<<<not xml")
        try {
            assertThrows<Exception> {
                XmlParser.parse(file, ::parseStringsXmlDom)
            }
        } finally {
            file.delete()
        }
    }
}
