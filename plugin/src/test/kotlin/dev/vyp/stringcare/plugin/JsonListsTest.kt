package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.parseJsonStringList
import dev.vyp.stringcare.plugin.internal.toJsonStringList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonListsTest {
    @Test
    fun `round-trip string lists through JSON`() {
        val list = listOf("src/main", "strings.xml", "path with space")
        val json = toJsonStringList(list)
        assertEquals(list, parseJsonStringList(json))
    }

    @Test
    fun `empty list parses`() {
        assertEquals(emptyList(), parseJsonStringList("[]"))
    }
}
