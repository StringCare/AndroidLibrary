package dev.vyp.stringcare.plugin.infrastructure.crypto

import dev.vyp.stringcare.plugin.internal.Stark
import dev.vyp.stringcare.plugin.internal.LoadResult
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

class ObfuscationServiceTest {
    @Test
    fun `obfuscate and reveal roundtrip when native is loaded`() {
        assumeTrue(Stark.isNativeLibLoaded() is LoadResult.Loaded)
        val svc = ObfuscationService()
        val key = "k"
        val plain = "hello-世界".toByteArray(Charsets.UTF_8)
        val appId = "com.example.app"
        val obf = svc.obfuscate(key, plain, appId)
        assertTrue(obf.isNotEmpty())
        assertFalse(obf.contentEquals(plain), "Obfuscated bytes must differ from original bytes")
        val back = svc.reveal(key, obf, appId)
        assertArrayEquals(plain, back)
    }
}
