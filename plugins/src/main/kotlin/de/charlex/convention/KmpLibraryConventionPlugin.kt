package de.charlex.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.plugins.ExtensionAware
import de.charlex.convention.config.configureIosTargets
import de.charlex.convention.config.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Kotlin Multiplatform library targeting Android and iOS. No JVM target.
 *
 * Creates the Android and iOS targets itself, so a consuming module declares no
 * platforms of its own -- only its `namespace`, which ends up in the published
 * AAR as the R class package.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            val compileSdk = libs.version("compileSdk").toInt()
            val minSdk = libs.version("minSdk").toInt()

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()
                configureIosTargets()

                val androidLibrary = (this as ExtensionAware)
                    .extensions
                    .getByName("androidLibrary") as KotlinMultiplatformAndroidLibraryExtension

                androidLibrary.compileSdk = compileSdk
                androidLibrary.minSdk = minSdk
            }

            configureKotlinMultiplatform()
        }
    }
}
