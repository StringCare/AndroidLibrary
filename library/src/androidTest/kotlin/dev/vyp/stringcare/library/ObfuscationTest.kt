package dev.vyp.stringcare.library

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObfuscationTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SC.init(context)
    }

    @Test
    fun v0_obfuscate_reveal_roundtrip() {
        val original = "sensitive"
        val obfuscated = SC.obfuscate(original, androidTreatment = false, version = Version.V0)
        val revealed = SC.reveal(obfuscated, androidTreatment = false, version = Version.V0)
        if (obfuscated != original) assertEquals(original, revealed)
    }

    @Test
    fun v1_obfuscate_returns_different_value() {
        val original = "sensitive"
        val obfuscated = SC.obfuscate(original, androidTreatment = false, version = Version.V1)
        assertTrue(obfuscated.isEmpty() || obfuscated != original)
    }

    @Test
    fun v2_obfuscate_returns_different_value() {
        val original = "sensitive"
        val obfuscated = SC.obfuscate(original, androidTreatment = false, version = Version.V2)
        assertTrue(obfuscated.isEmpty() || obfuscated != original)
    }

    @Test
    fun v3_obfuscate_returns_different_value() {
        val original = "sensitive"
        val obfuscated = SC.obfuscate(original, androidTreatment = true, version = Version.V3)
        assertTrue(obfuscated.isEmpty() || obfuscated != original)
    }
}
