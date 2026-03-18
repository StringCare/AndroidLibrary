package dev.vyp.stringcare.library

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class FingerprintTest {

    @Test
    fun `generateKey produces key of length 16 bytes`() {
        val key = generateKey("test-key")
        assertNotNull(key.encoded)
        assertEquals(16, key.encoded.size)
    }

    @Test
    fun `generateKey produces different keys for different inputs`() {
        val key1 = generateKey("key1")
        val key2 = generateKey("key2")
        assertEquals(16, key1.encoded.size)
        assertEquals(16, key2.encoded.size)
        assert(!key1.encoded.contentEquals(key2.encoded))
    }
}
