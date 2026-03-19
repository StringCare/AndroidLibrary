package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.normalize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConfigurationNormalizationTest {
    @Test
    fun `String normalize collapses whitespace and newlines`() {
        assertEquals("a b c", "a\nb   c".normalize())
    }

    @Test
    fun `String normalize is idempotent for collapsed input`() {
        val s = "one two"
        assertEquals(s, s.normalize())
    }

    @Test
    fun `StringCareConfiguration normalize is idempotent on paths`() {
        val c =
            StringCareConfiguration("app").apply {
                srcFolders.clear()
                srcFolders.add("src//main")
                stringFiles.clear()
                stringFiles.add("strings.xml")
            }
        c.normalize()
        val first = c.srcFolders.toList() to c.stringFiles.toList()
        c.normalize()
        val second = c.srcFolders.toList() to c.stringFiles.toList()
        assertEquals(first, second)
    }
}
