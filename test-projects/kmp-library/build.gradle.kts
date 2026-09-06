plugins {
    id("de.charlex.convention.kmp.library")
    id("de.charlex.convention.publishing")
}

kotlin {
    androidLibrary {
        namespace = "de.charlex.testproject.kmp"
    }
}

mavenPublishConfig {
    name = "test-kmp-library"
    description = "Test project: Kotlin Multiplatform library with Android and iOS targets."
}
