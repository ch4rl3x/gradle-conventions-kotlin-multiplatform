import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
}

java {
    // Hardcoded to the JDK bundled with Android Studio.
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdk.get()))
    }
}

dependencies {
    // compileOnly: AGP and KGP are declared by the consuming build, which owns
    // their versions. Dokka and nexus-publish are implementation details of the
    // publishing conventions, so they ship with the plugins instead.
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    implementation(libs.nexusPublish.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("publishingRepository") {
            id = "de.charlex.convention.publishing.repository"
            implementationClass = "de.charlex.convention.PublishingRepositoryConventionPlugin"
        }
        register("androidLibrary") {
            id = "de.charlex.convention.android.library"
            implementationClass = "de.charlex.convention.AndroidLibraryConventionPlugin"
        }
        register("jvmLibrary") {
            id = "de.charlex.convention.jvm.library"
            implementationClass = "de.charlex.convention.JvmLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "de.charlex.convention.kmp.library"
            implementationClass = "de.charlex.convention.KmpLibraryConventionPlugin"
        }
        register("publishing") {
            id = "de.charlex.convention.publishing"
            implementationClass = "de.charlex.convention.PublishingConventionPlugin"
        }
    }
}
