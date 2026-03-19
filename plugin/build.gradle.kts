plugins {
    kotlin("jvm") version "2.0.21"
    id("java-gradle-plugin")
    id("maven-publish")
    id("signing")
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
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
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
 * Prebuilt natives from stringcare-android-c (submodule `stringcare-jni`).
 * NOTE: With `includeBuild("plugin")`, this project's `rootProject` is the plugin dir, not stringcare-android —
 * use a path relative to this project (`../stringcare-jni/dist`).
 */
val stringcareJniDist = layout.projectDirectory.dir("../stringcare-jni/dist")
val pluginJniSource = layout.projectDirectory.dir("src/main/kotlin/dev/vyp/stringcare/plugin/internal/jni")
val pluginNativesGenerated = layout.buildDirectory.dir("generated/stringcare-plugin-natives")

/**
 * Packs all host natives into the plugin JAR on every `processResources` / `jar`.
 * - If `../stringcare-jni/dist/{macos,linux,windows}/` exists (after building in stringcare-android-c), copies from there.
 * - Otherwise falls back to checked-in files under `internal/jni/`.
 */
tasks.register<Sync>("preparePluginNativeLibraries") {
    into(pluginNativesGenerated)
    if (stringcareJniDist.asFile.exists()) {
        from(stringcareJniDist.dir("macos")) { include("*.dylib") }
        from(stringcareJniDist.dir("linux")) { include("*.so") }
        from(stringcareJniDist.dir("windows")) { include("*.dll") }
    } else {
        from(pluginJniSource) {
            include("*.dylib", "*.dll", "*.so")
        }
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn("preparePluginNativeLibraries")
    from(pluginNativesGenerated) {
        include("*.dylib", "*.dll", "*.so")
    }
}

/**
 * Optional: copy dist natives into `src/.../jni/` for committing prebuilts to Git (e.g. Maven publish without submodule on CI).
 */
tasks.register<Copy>("syncPluginNativeLibraries") {
    val dist = stringcareJniDist
    into(pluginJniSource)
    from(dist.dir("macos")) { include("*.dylib") }
    from(dist.dir("linux")) { include("*.so") }
    from(dist.dir("windows")) { include("*.dll") }
    doFirst {
        if (!dist.asFile.exists()) {
            throw GradleException(
                "stringcare-jni/dist not found. Init submodule: git submodule update --init --recursive " +
                    "then build natives in stringcare-android-c (see that repo's docs)."
            )
        }
    }
}
