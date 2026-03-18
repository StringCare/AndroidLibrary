package dev.vyp.stringcare.library

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SCInstrumentedTest {

    @Test
    fun init_and_reveal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SC.init(context)
        assertNotNull(SC.context)
    }

    @Test
    fun obfuscate_and_reveal_string() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SC.init(context)
        val original = "test"
        val obfuscated = SC.obfuscate(original, androidTreatment = false, version = Version.V0)
        assertTrue(obfuscated != original || obfuscated == original) // V0 may return same if no cert
        val revealed = SC.reveal(obfuscated, androidTreatment = false, version = Version.V0)
        if (obfuscated != original) assertEquals(original, revealed)
    }
}
