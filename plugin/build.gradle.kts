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
            implementationClass = "StringCare"
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

signing {
    useGpgCmd()
    sign(publishing.publications["plugin"])
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from("src/main/kotlin/components/jni") {
        include("*.dylib", "*.dll")
    }
}
