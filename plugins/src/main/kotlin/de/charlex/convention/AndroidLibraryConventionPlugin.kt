package de.charlex.convention

import com.android.build.api.dsl.LibraryExtension
import de.charlex.convention.config.configureJava
import de.charlex.convention.config.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Android library defaults.
 *
 * When the Gradle property `convention.android.namespacePrefix` is set, the
 * namespace is derived from it and the module name -- prefix `com.example` and
 * module `my-library` give `com.example.my.library`. Without the property the
 * namespace is left to the module, since there is no sensible way to guess it.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            val namespacePrefix = providers.gradleProperty(NAMESPACE_PREFIX_PROPERTY).orNull

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                if (namespacePrefix != null) {
                    namespace = "$namespacePrefix.${project.name.replace("-", ".")}"
                }
            }

            configureJava()
        }
    }

    private companion object {
        const val NAMESPACE_PREFIX_PROPERTY = "convention.android.namespacePrefix"
    }
}
