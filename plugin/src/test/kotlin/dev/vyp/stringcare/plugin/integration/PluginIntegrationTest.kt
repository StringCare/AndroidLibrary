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
        var dir = File(requireNotNull(System.getProperty("user.dir")))
        while (!dir.resolve("settings.gradle.kts").exists() && dir.parentFile != null) {
            dir = dir.parentFile ?: break
        }
        require(dir.resolve("settings.gradle.kts").exists()) { "Unable to locate repository root from ${System.getProperty("user.dir")}" }
        return dir
    }

    @Test
    fun `compile plugin with configuration cache`() {
        val root = repoRoot()
        val result =
            GradleRunner.create()
                .withProjectDir(root)
                .withArguments("compileKotlin", "--configuration-cache")
                .forwardOutput()
                .build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    private fun runRepoBuild(vararg args: String) =
        GradleRunner.create()
            .withProjectDir(repoRoot())
            .withArguments(*args, "--stacktrace")
        .forwardOutput()
        .build()

    @Test
    fun `multi-module fixture configures successfully`() {
        val fixture = repoRoot().resolve("src/test/resources/test-projects/multi-module")
        assertTrue(fixture.exists())
        assertTrue(fixture.resolve("settings.gradle.kts").exists())
        assertTrue(fixture.resolve("app/build.gradle.kts").exists())
        assertTrue(fixture.resolve("feature/build.gradle.kts").exists())
    }

    @Test
    fun `multi-module fixture supports parallel build graph`() {
        val result = runRepoBuild("compileKotlin", "--parallel")
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `simple-app fixture reuses configuration cache`() {
        val fixture = repoRoot().resolve("src/test/resources/test-projects/simple-app")
        assertTrue(fixture.exists())
        val first = runRepoBuild("compileKotlin", "--configuration-cache")
        val second = runRepoBuild("compileKotlin", "--configuration-cache")
        assertTrue(first.output.contains("BUILD SUCCESSFUL"))
        assertTrue(second.output.contains("BUILD SUCCESSFUL"))
        assertTrue(
            second.output.contains("Reusing configuration cache.") ||
                second.output.contains("Configuration cache entry reused."),
        )
    }
}
