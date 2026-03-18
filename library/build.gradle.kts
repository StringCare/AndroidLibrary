plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "dev.vyp.stringcare.library"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-fexceptions"
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("CMakeLists.txt")
        }
    }
    ndkVersion = "26.1.10909125"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation(libs.espresso.core) {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    implementation(libs.androidx.appcompat)
    implementation(libs.commons.lang3)
    testImplementation("junit:junit:4.13.2")  // TODO: migrate to JUnit 5 in Phase 5
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
}

group = "dev.vyp.stringcare"
version = libs.versions.stringcare.get()

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "library"
                groupId = "dev.vyp.stringcare"
                version = libs.versions.stringcare.get()
                pom {
                    name.set("StringCare Android Library")
                    description.set("StringCare Android library for string obfuscation at runtime")
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
    signing {
        useGpgCmd()
        sign(publishing.publications["release"])
    }
}
