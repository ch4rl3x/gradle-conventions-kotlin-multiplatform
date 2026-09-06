package de.charlex.convention.config

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import de.charlex.convention.conventionVersions

internal fun KotlinMultiplatformExtension.configureAndroidTarget() {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(project.conventionVersions.jvmTarget))
        }
    }
}

/**
 * Declares the iOS targets only. Configuring a framework binary is a concern of
 * the module that is actually linked from Xcode -- it calls `binaries.framework`
 * itself, where it can also set baseName and exports.
 */
internal fun KotlinMultiplatformExtension.configureIosTargets() {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
