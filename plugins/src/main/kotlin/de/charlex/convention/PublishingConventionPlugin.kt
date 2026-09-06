package de.charlex.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import groovy.util.Node
import javax.inject.Inject

/**
 * Publishes a module to Maven Central: sources and javadoc jars, POM, signing.
 *
 * Configure it through the `mavenPublishConfig` extension; `url`, `scm` and the
 * licence have defaults, so usually only `name` and `description` are needed.
 *
 * Handles Kotlin Multiplatform, plain Android libraries and plain JVM libraries.
 */
class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create(
            "mavenPublishConfig",
            MavenPublishExtension::class.java
        )

        with(pluginManager) {
            apply("maven-publish")
            apply("signing")
            apply("org.jetbrains.dokka")
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                if (pluginManager.hasPlugin("com.android.library")) {
                    androidTarget {
                        publishLibraryVariants("release")
                    }
                }
            }
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant(ANDROID_RELEASE_VARIANT) {
                        withSourcesJar()
                    }
                }
            }

            extensions.configure<PublishingExtension> {
                publications.register(ANDROID_RELEASE_VARIANT, MavenPublication::class.java) {
                    afterEvaluate {
                        from(components.getByName(ANDROID_RELEASE_VARIANT))
                        artifactId = extension.artifactId ?: project.name
                    }
                }
            }
        }

        pluginManager.withPlugin("java-library") {
            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
            }

            extensions.configure<PublishingExtension> {
                publications.register(JVM_PUBLICATION, MavenPublication::class.java) {
                    from(components.getByName("java"))
                    afterEvaluate { artifactId = extension.artifactId ?: project.name }
                }
            }
        }

        val repositoryUrl = originUrl()

        val javadocJar = tasks.register("javadocJar", Jar::class.java) {
            archiveClassifier.set("javadoc")
            from(tasks.getByName("dokkaGeneratePublicationHtml"))
        }

        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                artifact(javadocJar)
                pom {
                    name.set(project.provider { extension.name ?: project.name })
                    description.set(project.provider { extension.description ?: project.description })
                    url.set(project.provider { extension.url ?: repositoryUrl })

                    licenses {
                        license {
                            name.set(project.provider { extension.licenseName })
                            url.set(project.provider { extension.licenseUrl })
                        }
                    }

                    scm {
                        connection.set(project.provider {
                            extension.scm.connection ?: repositoryUrl?.let(::scmConnection)
                        })
                        developerConnection.set(project.provider {
                            extension.scm.developerConnection ?: repositoryUrl?.let(::scmDeveloperConnection)
                        })
                        url.set(project.provider { extension.scm.url ?: repositoryUrl })
                    }

                    withXml {
                        if (extension.developers.isNotEmpty()) {
                            val root = asNode()
                            val existing = root.children().firstOrNull { child ->
                                child is Node && child.name() == "developers"
                            } as Node?
                            val devsNode = existing ?: root.appendNode("developers")
                            extension.developers.forEach { dev ->
                                val devNode = devsNode.appendNode("developer")
                                dev.id?.let { devNode.appendNode("id", it) }
                                dev.name?.let { devNode.appendNode("name", it) }
                                dev.email?.let { devNode.appendNode("email", it) }
                            }
                        }
                    }
                }
            }
        }

        extensions.configure<SigningExtension> {
            val signingKey = getLocalProperty("SIGNING_KEY") ?: System.getenv("SIGNING_KEY")
            if (signingKey != null) {
                useInMemoryPgpKeys(
                    getLocalProperty("SIGNING_KEY_ID") ?: System.getenv("SIGNING_KEY_ID"),
                    signingKey,
                    getLocalProperty("SIGNING_KEY_PASSWORD") ?: System.getenv("SIGNING_KEY_PASSWORD"),
                )
                val publishing = extensions.getByType<PublishingExtension>()
                sign(publishing.publications)
            }
        }


        tasks.withType<AbstractPublishToMaven>().configureEach {
            val signingTasks = tasks.withType<Sign>()
            mustRunAfter(signingTasks)
        }
    }

    private companion object {
        const val ANDROID_RELEASE_VARIANT = "release"
        const val JVM_PUBLICATION = "maven"
    }
}

abstract class MavenPublishExtension @Inject constructor(objects: ObjectFactory) {
    var name: String?
        get() = nameProperty.orNull
        set(value) = nameProperty.set(value)
    private val nameProperty: Property<String> = objects.property(String::class.java)

    var description: String?
        get() = descriptionProperty.orNull
        set(value) = descriptionProperty.set(value)
    private val descriptionProperty: Property<String> = objects.property(String::class.java)

    var url: String?
        get() = urlProperty.orNull
        set(value) = urlProperty.set(value)
    private val urlProperty: Property<String> = objects.property(String::class.java)

    /**
     * Maven artifactId. Defaults to the module name -- set it when the module is
     * named differently from the artifact, e.g. module `:billing` published as
     * `billing-suspend`.
     */
    var artifactId: String?
        get() = artifactIdProperty.orNull
        set(value) = artifactIdProperty.set(value)
    private val artifactIdProperty: Property<String> = objects.property(String::class.java)

    /** SPDX-style licence name. Defaults to Apache-2.0. */
    var licenseName: String
        get() = licenseNameProperty.get()
        set(value) = licenseNameProperty.set(value)
    private val licenseNameProperty: Property<String> =
        objects.property(String::class.java).convention("Apache-2.0 License")

    var licenseUrl: String
        get() = licenseUrlProperty.get()
        set(value) = licenseUrlProperty.set(value)
    private val licenseUrlProperty: Property<String> =
        objects.property(String::class.java).convention("https://www.apache.org/licenses/LICENSE-2.0.txt")

    val developers: MutableList<DeveloperConfig> = mutableListOf()
    fun developers(action: Action<in DeveloperContainer>) {
        val container = DeveloperContainer()
        action.execute(container)
        developers.addAll(container.developers)
    }

    val scm: SCMConfig = objects.newInstance(SCMConfig::class.java)
    fun scm(action: Action<in SCMConfig>) {
        action.execute(scm)
    }

    abstract class SCMConfig @Inject constructor(objects: ObjectFactory) {
        var connection: String?
            get() = connectionProperty.orNull
            set(value) = connectionProperty.set(value)
        private val connectionProperty: Property<String> = objects.property(String::class.java)

        var developerConnection: String?
            get() = developerConnectionProperty.orNull
            set(value) = developerConnectionProperty.set(value)
        private val developerConnectionProperty: Property<String> = objects.property(String::class.java)

        var url: String?
            get() = urlProperty.orNull
            set(value) = urlProperty.set(value)
        private val urlProperty: Property<String> = objects.property(String::class.java)
    }

    class DeveloperContainer {
        internal val developers = mutableListOf<DeveloperConfig>()
        fun developer(action: Action<in DeveloperConfig>) {
            val dev = DeveloperConfig()
            action.execute(dev)
            developers.add(dev)
        }
    }

    class DeveloperConfig {
        var id: String? = null
        var name: String? = null
        var email: String? = null
    }
}