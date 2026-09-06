package de.charlex.convention.config

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import de.charlex.convention.conventionVersions
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.io.File

fun Project.configureKotlinAndroid(extension: CommonExtension<*, *, *, *, *, *>) {
    val versions = conventionVersions

    if (extension is LibraryExtension) {
        extension.defaultConfig {
            consumerProguardFiles += File("consumer-rules.pro")
        }
    } else if (extension is ApplicationExtension) {
        extension.apply {
            defaultConfig {
                targetSdk = versions.targetSdk
            }
        }
    }

    extension.apply {
        compileSdk = versions.compileSdk

        defaultConfig {
            minSdk = versions.minSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // Can remove this once https://issuetracker.google.com/issues/260059413 is fixed.
        // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(versions.jvmTarget)
            targetCompatibility = JavaVersion.toVersion(versions.jvmTarget)
        }
    }
}
