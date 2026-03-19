plugins {
    id("org.owasp.dependencycheck") version "12.2.0"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

/** Dev: copies `stringcare-jni/dist` into the plugin’s `internal/jni` (included build `plugin`). */
tasks.register("syncPluginNativesFromDist") {
    group = "stringcare"
    description =
        "Copy stringcare-jni/dist → plugin internal/jni. Run after building natives in stringcare-android-c."
    dependsOn(gradle.includedBuild("plugin").task(":syncDistNativesToPluginJni"))
}
