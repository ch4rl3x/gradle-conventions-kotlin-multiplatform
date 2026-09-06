package de.charlex.convention

import de.charlex.convention.config.configureJava
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plain JVM library: `java-library` plus the shared Java toolchain.
 *
 * Add `org.jetbrains.kotlin.jvm` on top for a Kotlin/JVM library -- this plugin
 * deliberately does not apply it, so Java-only modules work too.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        configureJava()
    }
}
