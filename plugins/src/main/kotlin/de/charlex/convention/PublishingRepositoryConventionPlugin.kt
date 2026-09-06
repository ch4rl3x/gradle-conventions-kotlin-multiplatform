package de.charlex.convention

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.net.URI

/**
 * The staging repository the whole build publishes through.
 *
 * Apply it to the root project. Counterpart to [PublishingConventionPlugin],
 * which decides what an individual module publishes.
 *
 * Credentials come from local.properties first, then the environment, so the same
 * setup works locally and in CI.
 */
class PublishingRepositoryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        check(this == rootProject) {
            "de.charlex.convention.publishing.repository has to be applied to the root project, not to ${project.path}"
        }

        pluginManager.apply("io.github.gradle-nexus.publish-plugin")

        extensions.configure<NexusPublishExtension> {
            repositories {
                sonatype {
                    nexusUrl.set(URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/"))
                    snapshotRepositoryUrl.set(URI.create("https://central.sonatype.com/repository/maven-snapshots/"))
                    stagingProfileId.set(getLocalProperty("SONATYPE_STAGING_PROFILE_ID") ?: System.getenv("SONATYPE_STAGING_PROFILE_ID"))
                    username.set(getLocalProperty("OSSRH_USERNAME") ?: System.getenv("OSSRH_USERNAME"))
                    password.set(getLocalProperty("OSSRH_PASSWORD") ?: System.getenv("OSSRH_PASSWORD"))
                }
            }
        }
    }
}
