import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("java-gradle-plugin")
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    jacoco
}

gradlePlugin {
    plugins {
        create("stringcare") {
            id = "dev.vyp.stringcare.plugin"
            implementationClass = "dev.vyp.stringcare.plugin.StringCarePlugin"
            displayName = "StringCare"
            description = "StringCare Gradle plugin for Android string/asset obfuscation"
        }
    }
}

group = "dev.vyp.stringcare"
version = "5.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    compileOnly("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation(gradleTestKit())
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        if (name == "test") {
            excludeTags("integration")
        }
    }
}

val sourceSets = extensions.getByType(SourceSetContainer::class.java)
tasks.register<Test>("integrationTest") {
    group = "verification"
    description =
        "Runs @Tag(\"integration\") tests (git clone KotlinSample, nested Gradle). " +
            "Optional; default :plugin:test excludes these."
    testClassesDirs = sourceSets.named("test").get().output.classesDirs
    classpath = sourceSets.named("test").get().runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class.java) {
        isEnabled = true
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

/** Soft gate: raise minimum over time (target &gt;80% per roadmap). */
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.12".toBigDecimal()
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files(layout.projectDirectory.file("config/detekt/detekt.yml")))
    baseline = file("config/detekt/baseline.xml")
    parallel = true
}

ktlint {
    version.set("1.3.1")
    ignoreFailures.set(true)
    filter {
        exclude("**/build/**")
        exclude("**/Vars.kt")
    }
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            from(components["java"])
            artifactId = "plugin"
            groupId = "dev.vyp.stringcare"
            version = "5.0.0"
            pom {
                name.set("StringCare Gradle Plugin")
                description.set("StringCare Android Gradle plugin for compile-time obfuscation")
                url.set("https://github.com/vypdev/stringcare-android")
                scm {
                    connection.set("scm:git:git://github.com/vypdev/stringcare-android.git")
                    developerConnection.set("scm:git:ssh://github.com/vypdev/stringcare-android.git")
                    url.set("https://github.com/vypdev/stringcare-android")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("efraespada")
                        name.set("efraespada")
                        email.set("efraespada@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("nexusUsername") as String?
                password = project.findProperty("nexusPassword") as String?
            }
        }
    }
}

if (project.hasProperty("signing.gnupg.keyName")) {
    signing {
        useGpgCmd()
        sign(publishing.publications["plugin"])
    }
}

/**
 * Prebuilts from stringcare-android-c (Git submodule path in this repo: `stringcare-jni`).
 * Relative to `plugin/`: `../stringcare-jni/dist` (same tree as `dist/` in stringcare-android-c: `macos/`, `linux/`, `windows/`, …).
 */
val stringcareJniDist = layout.projectDirectory.dir("../stringcare-jni/dist")
val pluginJniSource = layout.projectDirectory.dir("src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni")
val pluginNativesGenerated = layout.buildDirectory.dir("generated/stringcare-plugin-natives")

/**
 * Mirrors `dist/` (or checked-in `internal/jni/`) into the build dir so `processResources` packs the same layout into the JAR.
 */
tasks.register<Sync>("preparePluginNativeLibraries") {
    into(pluginNativesGenerated)
    if (stringcareJniDist.asFile.exists()) {
        from(stringcareJniDist)
    } else {
        from(pluginJniSource)
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn("preparePluginNativeLibraries")
    from(pluginNativesGenerated) {
        include("**/*.dylib", "**/*.dll", "**/*.so")
    }
}

// If you run `:plugin:clean :plugin:jar` in one invocation, Gradle may run `jar` (UP-TO-DATE) before `clean` and delete the JAR. Force correct order.
tasks.named("jar") {
    mustRunAfter(tasks.named("clean"))
}

/**
 * Dev-only: mirror `../stringcare-jni/dist/` into `internal/jni/` (same folder layout: `macos/`, `linux/`, `windows/`, …).
 *
 * From repo root: `./gradlew syncPluginNativesFromDist`. Or: `./gradlew -p plugin syncDistNativesToPluginJni`
 */
tasks.register<Sync>("syncDistNativesToPluginJni") {
    group = "stringcare"
    description = "Dev: sync stringcare-jni/dist → plugin internal/jni (entire tree)."
    into(pluginJniSource)
    from(stringcareJniDist)
    doFirst {
        if (!stringcareJniDist.asFile.exists()) {
            throw GradleException(
                "stringcare-jni/dist not found. Init submodule: git submodule update --init --recursive " +
                    "then build natives in stringcare-android-c (see that repo's docs).",
            )
        }
    }
}

// Legacy name; prefer syncDistNativesToPluginJni.
tasks.register("syncPluginNativeLibraries") {
    group = "stringcare"
    description = "Alias for syncDistNativesToPluginJni."
    dependsOn("syncDistNativesToPluginJni")
}
