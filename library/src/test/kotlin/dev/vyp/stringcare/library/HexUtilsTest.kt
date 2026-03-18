package dev.vyp.stringcare.library

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HexUtilsTest {

    @Test
    fun `asHexUpper formats bytes correctly`() {
        val bytes = byteArrayOf(0x0A.toByte(), 0x0B.toByte(), 0xFF.toByte())
        assertEquals("0A:0B:FF", bytes.asHexUpper)
    }

    @Test
    fun `asHexLower formats bytes correctly`() {
        val bytes = byteArrayOf(0x0A.toByte(), 0x0B.toByte(), 0xFF.toByte())
        assertEquals("0a:0b:ff", bytes.asHexLower)
    }

    @Test
    fun `hexAsByteArray parses contiguous hex string`() {
        val hex = "0102AB"
        val bytes = hex.hexAsByteArray
        assertEquals(3, bytes.size)
        assertEquals(0x01.toByte(), bytes[0])
        assertEquals(0x02.toByte(), bytes[1])
        assertEquals(0xAB.toByte(), bytes[2])
    }

    @Test
    fun `escape escapes regex special characters`() {
        assertEquals("\\Q.\\E", ".".escape())
    }

    @Test
    fun `removeNewLines removes newlines`() {
        assertEquals("ab", "a\nb".removeNewLines())
    }

    @Test
    fun `androidTreatment trims and normalizes spaces`() {
        assertEquals("a b c", "  a   b   c  ".androidTreatment())
    }
}
