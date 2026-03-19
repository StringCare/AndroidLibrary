package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.domain.usecases.ExtractFingerprintUseCase
import dev.vyp.stringcare.plugin.internal.extractFingerprint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FingerprintExtractionTest {
    @Test
    fun `extractFingerprint reads SHA1 after variant block`() {
        val report =
            """
            > Task :app:signingReport
            :app:debug
            Variant debug
            Config: debug
            SHA1 DE:AD:BE:EF
            Valid until: forever
            """.trimIndent()
        val config = StringCareConfiguration("app").apply { debug = false }
        val key = report.extractFingerprint(module = "app", variant = "debug", configuration = config)
        assertTrue(key.contains("DE:AD:BE:EF", ignoreCase = true))
    }

    @Test
    fun `ExtractFingerprintUseCase finds SHA1 for nested module path`() {
        val report =
            """
            :feature:debug
            Variant debug
            SHA1 AA:BB:CC:DD
            """.trimIndent()
        val config = StringCareConfiguration("feature").apply { debug = false }
        val key =
            ExtractFingerprintUseCase.extract(
                reportText = report,
                module = "feature",
                variant = "debug",
                configuration = config,
            )
        assertTrue(key.contains("AA:BB:CC:DD", ignoreCase = true))
    }

    @Test
    fun `ExtractFingerprintUseCase returns empty when variant not matched`() {
        val report =
            """
            :app:release
            Variant release
            SHA1 11:22:33:44
            """.trimIndent()
        val config = StringCareConfiguration("app").apply { debug = false }
        val key =
            ExtractFingerprintUseCase.extract(
                reportText = report,
                module = "app",
                variant = "debug",
                configuration = config,
            )
        assertEquals("", key)
    }

    @Test
    fun `ExtractFingerprintUseCase returns empty for blank report`() {
        val config = StringCareConfiguration("app").apply { debug = false }
        assertEquals(
            "",
            ExtractFingerprintUseCase.extract(
                reportText = "",
                module = "app",
                variant = "debug",
                configuration = config,
            ),
        )
    }

    @Test
    fun `ExtractFingerprintUseCase with debug waits for valid until before returning key`() {
        val reportNoUntil =
            """
            :app:debug
            Variant debug
            SHA1 DE:AD:BE:EF
            """.trimIndent()
        val reportWithUntil =
            reportNoUntil + "\nValid until: Mon Jan 1 00:00:00 UTC 2040"
        val debugConfig = StringCareConfiguration("app").apply { debug = true }
        assertEquals(
            "",
            ExtractFingerprintUseCase.extract(
                reportText = reportNoUntil,
                module = "app",
                variant = "debug",
                configuration = debugConfig,
            ),
        )
        assertTrue(
            ExtractFingerprintUseCase.extract(
                reportText = reportWithUntil,
                module = "app",
                variant = "debug",
                configuration = debugConfig,
            ).contains("DE:AD:BE:EF", ignoreCase = true),
        )
    }
}
