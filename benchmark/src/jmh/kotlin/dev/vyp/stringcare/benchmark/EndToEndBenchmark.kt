package dev.vyp.stringcare.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.Base64

@State(Scope.Benchmark)
open class EndToEndBenchmark {
    @Param("100", "1000")
    var sampleSize: Int = 100

    private lateinit var sample: List<String>
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    @Setup
    fun setup() {
        sample =
            List(sampleSize) { index ->
                "String value #$index"
            }
    }

    @Benchmark
    fun obfuscateAndRevealBatch(): Int {
        val obfuscated = sample.map { encoder.encodeToString(it.toByteArray()) }
        val revealed = obfuscated.map { String(decoder.decode(it)) }
        return revealed.count { it.startsWith("String value #") }
    }
}
