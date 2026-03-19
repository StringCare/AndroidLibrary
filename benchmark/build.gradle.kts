plugins {
    kotlin("jvm") version "2.0.21"
    id("me.champeau.jmh") version "0.7.2"
}

jmh {
    warmupIterations.set(2)
    iterations.set(5)
    fork.set(1)
    timeOnIteration.set("1s")
    benchmarkMode.set(listOf("avgt"))
    timeUnit.set("ms")
    resultFormat.set("JSON")
}

kotlin {
    jvmToolchain(17)
}
