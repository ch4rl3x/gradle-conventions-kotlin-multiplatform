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
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
    }
}

dependencies {
    // compileOnly: the consuming build brings these plugins itself, declared as
    // `apply false` in its root build script.
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.nexusPublish.gradlePlugin)
    compileOnly(libs.dokka.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("root") {
            id = "de.charlex.convention.root"
            implementationClass = "de.charlex.convention.RootConventionPlugin"
        }
        register("androidLibrary") {
            id = "de.charlex.convention.android.library"
            implementationClass = "de.charlex.convention.AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "de.charlex.convention.android.application"
            implementationClass = "de.charlex.convention.AndroidApplicationConventionPlugin"
        }
        register("kotlinMultiplatform") {
            id = "de.charlex.convention.kotlin.multiplatform"
            implementationClass = "de.charlex.convention.KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformMobile") {
            id = "de.charlex.convention.kotlin.multiplatform.mobile"
            implementationClass = "de.charlex.convention.KotlinMultiplatformMobileConventionPlugin"
        }
        register("centralPublish") {
            id = "de.charlex.convention.centralPublish"
            implementationClass = "de.charlex.convention.MavenCentralPublishConventionPlugin"
        }
    }
}
