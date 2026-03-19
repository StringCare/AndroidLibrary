package dev.vyp.stringcare.plugin.integration

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Lightweight checks that the composite build still configures with the configuration cache enabled.
 */
@Tag("integration")
class PluginIntegrationTest {
    private fun repoRoot(): File {
        var dir = File(requireNotNull(System.getProperty("user.dir")))
        while (
            !(dir.resolve("settings.gradle.kts").exists() &&
                dir.resolve("plugin").isDirectory &&
                dir.resolve("app").isDirectory) &&
            dir.parentFile != null
        ) {
            dir = dir.parentFile ?: break
        }
        require(
            dir.resolve("settings.gradle.kts").exists() &&
                dir.resolve("plugin").isDirectory &&
                dir.resolve("app").isDirectory,
        ) { "Unable to locate repository root from ${System.getProperty("user.dir")}" }
        return dir
    }

    @Test
    fun `compile plugin with configuration cache`() {
        val result = runRepoBuild(":plugin:compileKotlin", "--configuration-cache")
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    private fun runBuild(
        projectDir: File,
        vararg args: String,
    ): org.gradle.testkit.runner.BuildResult {
        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args, "--stacktrace")
            .forwardOutput()
            .build()
    }

    private fun runBuildWithGradleVersion(
        projectDir: File,
        gradleVersion: String,
        vararg args: String,
    ): org.gradle.testkit.runner.BuildResult {
        val runner =
            GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(*args, "--stacktrace")
                .forwardOutput()
                .withGradleVersion(gradleVersion)
        return runner.build()
    }

    private fun runRepoBuild(vararg args: String) =
        runBuild(repoRoot(), *args)

    private fun copyRepoForAgpCompatibilityTest(): File {
        val source = repoRoot()
        val temp = createTempDirectory(prefix = "stringcare-agp-compat-").toFile()
        // Copy only the pieces required to configure :app and :plugin.
        listOf("app", "plugin", "library", "buildSrc", "gradle").forEach { name ->
            source.resolve(name).copyRecursively(temp.resolve(name), overwrite = true)
        }
        listOf("settings.gradle.kts", "build.gradle.kts", "gradle.properties").forEach { fileName ->
            source.resolve(fileName).copyTo(temp.resolve(fileName), overwrite = true)
        }
        return temp
    }

    private fun setAgpVersion(
        copiedRepo: File,
        version: String,
    ) {
        val versionsFile = copiedRepo.resolve("gradle/libs.versions.toml")
        val updated = versionsFile.readText().replace(Regex("""^agp = ".*"$""", RegexOption.MULTILINE), """agp = "$version"""")
        versionsFile.writeText(updated)
    }

    private fun assertAppConfiguresWithAgp(version: String) {
        val copiedRepo = copyRepoForAgpCompatibilityTest()
        setAgpVersion(copiedRepo, version)
        val gradleVersion =
            when (version) {
                "9.0.0" -> "9.1.0"
                else -> null
            }
        val result =
            if (gradleVersion == null) {
                runBuild(copiedRepo, ":app:help")
            } else {
                runBuildWithGradleVersion(copiedRepo, gradleVersion, ":app:help")
            }
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `app configures with agp 8_7_3`() {
        assertAppConfiguresWithAgp("8.7.3")
    }

    @Test
    fun `app configures with agp 9_0_0`() {
        assertAppConfiguresWithAgp("9.0.0")
    }

    @Test
    fun `multi-module fixture configures successfully`() {
        val fixture = repoRoot().resolve("plugin/src/test/resources/test-projects/multi-module")
        assertTrue(fixture.exists())
        assertTrue(fixture.resolve("settings.gradle.kts").exists())
        assertTrue(fixture.resolve("app/build.gradle.kts").exists())
        assertTrue(fixture.resolve("feature/build.gradle.kts").exists())
    }

    @Test
    fun `multi-module fixture supports parallel build graph`() {
        val result = runRepoBuild(":plugin:compileKotlin", "--parallel")
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `simple-app fixture reuses configuration cache`() {
        val fixture = repoRoot().resolve("plugin/src/test/resources/test-projects/simple-app")
        assertTrue(fixture.exists())
        val first = runRepoBuild(":plugin:compileKotlin", "--configuration-cache")
        val second = runRepoBuild(":plugin:compileKotlin", "--configuration-cache")
        assertTrue(first.output.contains("BUILD SUCCESSFUL"))
        assertTrue(second.output.contains("BUILD SUCCESSFUL"))
        assertTrue(
            second.output.contains("Reusing configuration cache.") ||
                second.output.contains("Configuration cache entry reused."),
        )
    }
}
