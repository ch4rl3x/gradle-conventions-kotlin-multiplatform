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
 * The module sets its own `namespace` -- it ends up in the published AAR as the
 * R class package, so it is not derived from anything.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            configureJava()
        }
    }
}
