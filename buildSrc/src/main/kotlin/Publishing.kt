package dev.vyp.stringcare.build

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

private const val GROUP_ID = "dev.vyp.stringcare"

/**
 * Configures POM, Maven repositories and signing for an existing Maven publication.
 * The project must create the publication with from(components[...]) before calling this.
 * @param artifactId Maven artifactId (e.g. "library", "plugin")
 * @param description POM description
 * @param publicationName name of the publication (default "release"; use "plugin" for the Gradle plugin)
 */
fun Project.configurePublishing(
    artifactId: String,
    description: String,
    publicationName: String = "release"
) {
    group = GROUP_ID

    val publishing = extensions.getByType(PublishingExtension::class.java)
    publishing.repositories.maven {
        name = "sonatype"
        url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials {
            username = project.findProperty("nexusUsername") as String?
            password = project.findProperty("nexusPassword") as String?
        }
    }

    afterEvaluate {
        val pub = publishing.publications.findByName(publicationName) as? MavenPublication
            ?: return@afterEvaluate

        pub.groupId = GROUP_ID
        pub.artifactId = artifactId
        pub.version = project.findProperty("VERSION_NAME")?.toString()
            ?: project.version.toString()

        pub.pom {
            name.set("StringCare $artifactId")
            this.description.set(description)
            url.set("https://github.com/vypdev/stringcare-android")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("efraespada")
                    name.set("Efra Espada")
                    email.set("efraespada@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/vypdev/stringcare-android.git")
                developerConnection.set("scm:git:ssh://github.com/vypdev/stringcare-android.git")
                url.set("https://github.com/vypdev/stringcare-android")
            }
        }

        val signing = extensions.getByType(SigningExtension::class.java)
        signing.useGpgCmd()
        signing.sign(pub)
    }
}
