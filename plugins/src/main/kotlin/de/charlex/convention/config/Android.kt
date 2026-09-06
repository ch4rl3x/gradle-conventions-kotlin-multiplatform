package de.charlex.convention.config

import com.android.build.api.dsl.LibraryExtension
import de.charlex.convention.libs
import de.charlex.convention.version
import org.gradle.api.Project

internal fun Project.configureKotlinAndroid(extension: LibraryExtension) {
    extension.compileSdk = libs.version("compileSdk").toInt()
    extension.defaultConfig.minSdk = libs.version("minSdk").toInt()
    extension.defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}
