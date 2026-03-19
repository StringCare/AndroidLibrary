package dev.vyp.stringcare.plugin

import dev.vyp.stringcare.plugin.internal.defaultConfig
import dev.vyp.stringcare.plugin.internal.locateResourceFiles
import dev.vyp.stringcare.plugin.internal.tempPath
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Baseline checks for plugin wiring and filesystem helpers (no Android SDK required).
 */
class StringCareBaselineBehaviorTest {
    @Test
    fun `plugin applies and exposes stringcare extension`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("dev.vyp.stringcare.plugin")
        assertNotNull(project.extensions.findByName("stringcare"))
    }

    @Test
    fun `locateResourceFiles returns nothing for empty temp project`() {
        val root = tempPath()
        try {
            val dir = File(root, "empty-res").apply { mkdirs() }
            val config = defaultConfig()
            val found = locateResourceFiles(dir.absolutePath, config)
            assertTrue(found.isEmpty())
        } finally {
            StringCarePlugin.resetFolder()
        }
    }
}
