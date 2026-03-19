package dev.vyp.stringcare.plugin.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Lightweight checks that the composite build still configures with the configuration cache enabled.
 */
@Tag("integration")
class PluginIntegrationTest {
    private fun repoRoot(): File {
        var dir = File(System.getProperty("user.dir"))
        if (!dir.resolve("settings.gradle.kts").exists()) {
            dir = dir.parentFile
        }
        return dir
    }

    @Test
    fun `compile plugin with configuration cache`() {
        val root = repoRoot()
        val result =
            GradleRunner.create()
                .withProjectDir(root)
                .withArguments(":plugin:compileKotlin", "--configuration-cache", "--no-daemon")
                .forwardOutput()
                .build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
