import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.net.URI

plugins {
    alias(libs.plugins.nexusPublish)
}

group = "de.charlex.conventions.kmp"

// This repository builds the convention plugins, so it cannot apply them to
// itself -- Gradle cannot includeBuild the build it is currently running. The
// publishing setup below is therefore written out by hand, once.

fun localProperty(key: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    return java.util.Properties().apply { file.inputStream().use { load(it) } }.getProperty(key)
}

fun secret(key: String): String? = localProperty(key) ?: System.getenv(key)

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(URI.create("https://central.sonatype.com/repository/maven-snapshots/"))
            stagingProfileId.set(secret("SONATYPE_STAGING_PROFILE_ID"))
            username.set(secret("OSSRH_USERNAME"))
            password.set(secret("OSSRH_PASSWORD"))
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    // withPlugin instead of a direct configure(): the modules apply maven-publish
    // themselves, which happens after this block runs.
    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set("${rootProject.name} :: ${project.name}")
                    description.set("Shared Gradle convention plugins and version catalog for Kotlin Multiplatform libraries published to Maven Central.")
                    url.set("https://github.com/ch4rl3x/gradle-conventions-kotlin-multiplatform")

                    licenses {
                        license {
                            name.set("Apache-2.0 License")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("ch4rl3x")
                            name.set("Alexander Karkossa")
                            email.set("alexander.karkossa@googlemail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/ch4rl3x/gradle-conventions-kotlin-multiplatform.git")
                        developerConnection.set("scm:git:ssh://github.com/ch4rl3x/gradle-conventions-kotlin-multiplatform.git")
                        url.set("https://github.com/ch4rl3x/gradle-conventions-kotlin-multiplatform/tree/main")
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("signing") {
        val signingKey = secret("SIGNING_KEY")

        extensions.configure<SigningExtension> {
            // Without a key -- local builds, publishToMavenLocal -- signing stays
            // off instead of failing.
            if (signingKey != null) {
                useInMemoryPgpKeys(secret("SIGNING_KEY_ID"), signingKey, secret("SIGNING_KEY_PASSWORD"))
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }

        // https://github.com/gradle/gradle/issues/26091
        tasks.withType<AbstractPublishToMaven>().configureEach {
            mustRunAfter(tasks.withType<Sign>())
        }
    }
}
