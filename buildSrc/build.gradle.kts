plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Dependencies for build logic; gradleApi() is included by kotlin-dsl
// Do not add AGP/Kotlin here to avoid classpath conflict with version catalog
