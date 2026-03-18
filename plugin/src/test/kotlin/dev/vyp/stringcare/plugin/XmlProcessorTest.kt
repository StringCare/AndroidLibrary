package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.parseXML
import dev.vyp.stringcare.plugin.models.StringEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class XmlProcessorTest {

    @Test
    fun `parseXML extracts StringEntity with hidden true`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="public_string">Hello</string>
                <string name="secret" hidden="true">sensitive</string>
            </resources>
        """.trimIndent()
        val file = File.createTempFile("strings", ".xml")
        file.writeText(xml)
        try {
            val entities = parseXML(file)
            assertEquals(1, entities.size)
            val entity = entities[0]
            assertEquals("secret", entity.name)
            assertEquals("sensitive", entity.value)
            assertTrue(entity.androidTreatment)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `parseXML returns empty for no hidden strings`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="public_string">Hello</string>
            </resources>
        """.trimIndent()
        val file = File.createTempFile("strings", ".xml")
        file.writeText(xml)
        try {
            val entities = parseXML(file)
            assertTrue(entities.isEmpty())
        } finally {
            file.delete()
        }
    }
}
