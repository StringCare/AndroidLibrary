package dev.vyp.stringcare.plugin.internal

import java.util.concurrent.atomic.AtomicReference

/** Holds a temporary directory root for integration tests that clone sample projects. */
internal object StringCareSession {
    private val testTempRoot = AtomicReference<String?>(null)

    fun installTestTempRoot(path: String) {
        testTempRoot.set(path)
    }

    fun requireTestTempRoot(): String =
        testTempRoot.get() ?: error("Test temp root not set; call tempPath() first")

    fun clearTestTempRoot() {
        testTempRoot.set(null)
    }
}
