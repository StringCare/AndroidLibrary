package dev.vyp.stringcare.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.io.File
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.pathString

@State(Scope.Benchmark)
open class FileScanningBenchmark {
    @Param("100", "1000", "10000")
    var totalFiles: Int = 100

    private lateinit var projectRoot: File

    @Setup(Level.Trial)
    fun setup() {
        projectRoot = Files.createTempDirectory("scan-benchmark-").toFile()
        createFixtureTree(projectRoot, totalFiles)
    }

    @Benchmark
    fun locateResources(): Int =
        projectRoot
            .toPath()
            .toFile()
            .walkTopDown()
            .count { file ->
                file.isFile && file.name == "strings.xml" &&
                    !file.toPath().pathString.contains("/build/") &&
                    !file.toPath().pathString.contains("/.gradle/")
            }

    @Benchmark
    fun locateAssets(): Int =
        projectRoot
            .toPath()
            .toFile()
            .walkTopDown()
            .count { file ->
                file.isFile && file.toPath().name == "payload.bin" && file.toPath().extension == "bin"
            }

    private fun createFixtureTree(root: File, files: Int) {
        val module = File(root, "app/src/main")
        val resources = File(module, "res")
        val assets = File(module, "assets")
        resources.mkdirs()
        assets.mkdirs()

        val perType = files / 2
        repeat(perType) { idx ->
            val valuesDir = File(resources, "values-$idx")
            valuesDir.mkdirs()
            File(valuesDir, "strings.xml").writeText(
                """
                <resources>
                    <string name="v_$idx" hidden="true">value_$idx</string>
                </resources>
                """.trimIndent(),
            )
        }

        repeat(files - perType) { idx ->
            val nested = File(assets, "folder-$idx")
            nested.mkdirs()
            File(nested, "payload.bin").writeBytes(byteArrayOf(1, 2, 3, 4))
        }
    }
}
