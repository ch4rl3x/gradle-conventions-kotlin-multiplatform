package de.charlex.convention.config

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configureIosTargets() {
    iosArm64()
    iosSimulatorArm64()
}
