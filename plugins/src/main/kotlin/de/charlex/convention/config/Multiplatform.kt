package de.charlex.convention.config

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configureIosTargets() {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
