plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Dependencies for build logic can be added here when needed (e.g. Publishing.kt)
// Do not add AGP/Kotlin here to avoid classpath conflict with version catalog
